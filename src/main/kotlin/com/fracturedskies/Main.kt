package com.fracturedskies

import com.fracturedskies.engine.*
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.game.Game
import com.fracturedskies.game.TimeSystem
import com.fracturedskies.game.WorldGeneratorSystem
import com.fracturedskies.game.skylight.BlockLightSystem
import com.fracturedskies.game.skylight.SkyLightSystem
import com.fracturedskies.game.water.WaterSystem
import com.fracturedskies.game.workers.Delegator
import com.fracturedskies.render.RenderGameSystem
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.cancelChildren
import kotlinx.coroutines.experimental.runBlocking
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.CoroutineContext

class MainGameSystem(coroutineContext: CoroutineContext) {
  val game = Game(coroutineContext) { message ->
    when (message) {
      is RequestShutdown -> shutdownRequested.set(true)
    }
  }
  val channel get() = game.channel

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

fun main(args: Array<String>) = runBlocking<Unit> {
  UI_CONTEXT = coroutineContext

  // Subscribe all game systems
  val mainGameSystem = MainGameSystem(coroutineContext)
  register(mainGameSystem.channel)
  register(RenderGameSystem(coroutineContext + UI_CONTEXT).channel)
  register(WorldGeneratorSystem(coroutineContext + CommonPool).channel)
  register(Delegator(coroutineContext).channel)
  register(SkyLightSystem(coroutineContext).channel)
  register(BlockLightSystem(coroutineContext).channel)
  register(WaterSystem(coroutineContext).channel)
  register(TimeSystem(coroutineContext).channel)

  // Run game
  mainGameSystem.run(coroutineContext+CommonPool)
}
