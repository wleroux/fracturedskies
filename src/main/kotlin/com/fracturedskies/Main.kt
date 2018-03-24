package com.fracturedskies

import com.fracturedskies.api.*
import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.api.*
import com.fracturedskies.engine.collections.Dimension
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.render.RenderGameSystem
import kotlinx.coroutines.experimental.*
import java.util.*
import java.util.concurrent.TimeUnit.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.CoroutineContext

private const val MILLISECONDS_PER_UPDATE: Long = 16
private val NANOSECONDS_PER_UPDATE: Long = NANOSECONDS.convert(MILLISECONDS_PER_UPDATE, MILLISECONDS)
private const val SECONDS_PER_UPDATE: Float = MILLISECONDS_PER_UPDATE.toFloat() / 1000f

lateinit var UI_CONTEXT: CoroutineContext

fun main(args: Array<String>) = runBlocking {
  UI_CONTEXT = coroutineContext

  // Listen for Shutdown request
  val shutdownRequested = AtomicBoolean(false)
  register(MessageChannel(coroutineContext) { message ->
    if (message is RequestShutdown) shutdownRequested.set(true)
  })

  // Load Mods
  val modLoaders = ServiceLoader.load(ModLoader::class.java)
  modLoaders.forEach { modLoader -> modLoader.initialize(coroutineContext + CommonPool) }
  modLoaders.forEach { modLoader -> modLoader.start() }

  // Run Main Render Loop
  val renderGameSystem = RenderGameSystem(coroutineContext + CommonPool)
  register(renderGameSystem.channel)

  val renderJob = launch(coroutineContext + UI_CONTEXT) {
    renderGameSystem.initialize()
    while (!shutdownRequested.get()) {
      renderGameSystem.update()
      renderGameSystem.render()
      yield()
    }
    renderGameSystem.shutdown()
  }

  // Run Main Game Loop
  val updateJob = launch(coroutineContext + CommonPool) {
    send(Initialize(Cause.of(this))).await()
    send(NewGameRequested(Dimension(1 * CHUNK_X_SIZE, 16 * CHUNK_Y_SIZE, 1 * CHUNK_Z_SIZE), Cause.of(this)))
    var last = System.nanoTime()
    while (!shutdownRequested.get()) {
      val now = System.nanoTime()
      if (now - last >= NANOSECONDS_PER_UPDATE) {
        send(Update(SECONDS_PER_UPDATE, Cause.of(this))).await()
        last = now
      }
      yield()
    }
    send(Shutdown(Cause.of(this))).await()
  }

  updateJob.join()
  renderJob.join()
  coroutineContext.cancelChildren()
}
