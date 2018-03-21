package com.fracturedskies

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.api.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.send
import kotlinx.coroutines.experimental.*
import java.util.*
import java.util.concurrent.TimeUnit.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.CoroutineContext

class MainGameSystem(private val coroutineContext: CoroutineContext) {
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

  suspend fun run() {
    send(Initialize(Cause.of(this), MultiTypeMap())).await()
    var last = System.nanoTime()
    while (!shutdownRequested.get()) {
      val now = System.nanoTime()
      if (now - last >= NANOSECONDS_PER_UPDATE) {
        send(Update(SECONDS_PER_UPDATE, Cause.of(this), MultiTypeMap())).await()
        last = now
      }

      val alpha = (now - last).toFloat() / NANOSECONDS_PER_UPDATE.toFloat()
      send(Render(alpha, Cause.of(this), MultiTypeMap())).await()
    }

    send(Shutdown(Cause.of(this), MultiTypeMap())).await()
    coroutineContext.cancelChildren()
  }

  override fun toString() = this.javaClass.simpleName!!
}

lateinit var UI_CONTEXT: CoroutineContext
val DIMENSION = Dimension(128, 32, 128)

fun main(args: Array<String>) = runBlocking<Unit> {
  UI_CONTEXT = coroutineContext

  val modLoaders = ServiceLoader.load(ModLoader::class.java)
  val mainGameSystem = MainGameSystem(coroutineContext + CommonPool)
  register(mainGameSystem.channel)
  modLoaders.forEach { modLoader -> modLoader.initialize(coroutineContext + CommonPool) }
  modLoaders.forEach { modLoader -> modLoader.start() }

  mainGameSystem.run()
}
