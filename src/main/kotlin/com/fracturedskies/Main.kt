package com.fracturedskies

import com.fracturedskies.api.*
import com.fracturedskies.api.GameSpeed.NORMAL
import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.api.*
import com.fracturedskies.engine.collections.Dimension
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.engine.messages.MessageBus.waitForIdle
import com.fracturedskies.render.RenderGameSystem
import kotlinx.coroutines.experimental.*
import java.util.*
import java.util.concurrent.TimeUnit.*
import java.util.concurrent.atomic.*
import java.util.concurrent.locks.LockSupport.parkNanos

fun main(args: Array<String>) = runBlocking {
  // Listen for Shutdown request
  val shutdownRequested = AtomicBoolean(false)
  val gameSpeed = AtomicReference<GameSpeed>(NORMAL)
  register(MessageChannel(coroutineContext) { message ->
    if (message is ShutdownRequested) shutdownRequested.set(true)
    if (message is GameSpeedUpdated) gameSpeed.set(message.gameSpeed)
  })

  // Load Mods
  val modLoaders = ServiceLoader.load(ModLoader::class.java)
  modLoaders.forEach { modLoader -> modLoader.initialize(coroutineContext + CommonPool) }
  modLoaders.forEach { modLoader -> modLoader.start() }

  // Run Main Render Loop
  val renderGameSystem = RenderGameSystem(coroutineContext)
  register(renderGameSystem.channel)

  val renderJob = launch(coroutineContext) {
    renderGameSystem.glInitialize()
    while (!shutdownRequested.get()) {
      renderGameSystem.glUpdate()
      renderGameSystem.glRender()
      yield()
    }
    renderGameSystem.glShutdown()
  }

  // Run Main Game Loop
  val updateJob = launch(coroutineContext + CommonPool) {
    send(Initialize(Cause.of(this)))
    waitForIdle()

    send(NewGameRequested(Dimension(4 * 16, 8 * 16, 4 * 16), Cause.of(this)))
    waitForIdle()
    var last = System.nanoTime()
    while (!shutdownRequested.get()) {
      val now = System.nanoTime()
      if (now - last >= MILLISECONDS.toNanos(gameSpeed.get().msBetweenUpdates)) {
        send(Update(Cause.of(this)))
        waitForIdle()
        last = now
      } else {
        parkNanos(MICROSECONDS.toNanos(125L))
      }
    }
    send(Shutdown(Cause.of(this)))
    waitForIdle()
  }

  updateJob.join()
  renderJob.join()
  coroutineContext.cancelChildren()
}
