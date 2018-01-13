package com.fracturedskies

import com.fracturedskies.engine.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.game.render.RenderGameSystem
import com.fracturedskies.game.skylight.*
import com.fracturedskies.game.time.TimeSystem
import com.fracturedskies.game.water.WaterSystem
import com.fracturedskies.game.worker.WorkerSystem
import com.fracturedskies.game.worldgenerator.WorldGeneratorSystem
import kotlinx.coroutines.experimental.*
import java.util.concurrent.TimeUnit.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.CoroutineContext

class MainGameSystem(coroutineContext: CoroutineContext) {
  val channel = MessageChannel(coroutineContext) { message ->
    when (message) {
      is RequestShutdown -> shutdownRequested.set(true)
    }
  }

  companion object {
    private val MILLISECONDS_PER_UPDATE: Long = 16
    private val NANOSECONDS_PER_UPDATE: Long = NANOSECONDS.convert(MILLISECONDS_PER_UPDATE, MILLISECONDS)
    private val SECONDS_PER_UPDATE: Float = MILLISECONDS_PER_UPDATE.toFloat() / 1000f
  }

  private val shutdownRequested = AtomicBoolean(false)

  suspend fun run(coroutineContext: CoroutineContext) {
    send(Initialize(Cause.of(this), Context())).await()
    var last = System.nanoTime()
    while (!shutdownRequested.get()) {
      val now = System.nanoTime()
      if (now - last >= NANOSECONDS_PER_UPDATE) {
        send(Update(SECONDS_PER_UPDATE, Cause.of(this), Context())).await()
        last = now
      }

      val alpha = (now - last).toFloat() / NANOSECONDS_PER_UPDATE.toFloat()
      send(Render(alpha, Cause.of(this), Context())).await()
    }

    send(Shutdown(Cause.of(this), Context())).await()
    coroutineContext.cancelChildren()
  }
}

lateinit var UI_CONTEXT: CoroutineContext
val DIMENSION = Dimension(128, 256, 128)

fun main(args: Array<String>) = runBlocking<Unit> {
  UI_CONTEXT = coroutineContext

  // Subscribe all game systems
  val mainGameSystem = MainGameSystem(coroutineContext)
  register(mainGameSystem.channel)
  register(RenderGameSystem(coroutineContext + UI_CONTEXT).channel)
  register(WorldGeneratorSystem(coroutineContext + CommonPool, DIMENSION).channel)
  register(SkyLightSystem(coroutineContext, DIMENSION).channel)
  register(BlockLightSystem(coroutineContext, DIMENSION).channel)
  register(WaterSystem(coroutineContext, DIMENSION).channel)
  register(TimeSystem(coroutineContext).channel)
  register(WorkerSystem(coroutineContext, DIMENSION).channel)

  // Run game
  mainGameSystem.run(coroutineContext+CommonPool)
}
