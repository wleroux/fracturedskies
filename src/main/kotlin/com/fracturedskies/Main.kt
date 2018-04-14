package com.fracturedskies

import com.fracturedskies.api.*
import com.fracturedskies.api.GameSpeed.UNLIMITED
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
import java.util.concurrent.Executors.*
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit.*
import java.util.concurrent.atomic.*
import java.util.concurrent.locks.LockSupport.parkNanos

enum class GameSize(val dimension: Dimension) {
  MINI(Dimension(CHUNK_X_SIZE, 16 * CHUNK_Y_SIZE, CHUNK_Z_SIZE)), // 16 chunks, 65536 blocks
  SMALL(Dimension(4 * CHUNK_X_SIZE, 16 * CHUNK_Y_SIZE, 4 * CHUNK_Z_SIZE)), // 256 chunks, 1048576 blocks
  NORMAL(Dimension(8 * CHUNK_X_SIZE, 16 * CHUNK_Y_SIZE, 8 * CHUNK_Z_SIZE)), // 1024 chunks, 4194304 blocks
  LARGE(Dimension(12 * CHUNK_X_SIZE, 16 * CHUNK_Y_SIZE, 12 * CHUNK_Z_SIZE)), // 2304 chunks, 9437184 blocks
  MEGA(Dimension(16 * CHUNK_X_SIZE, 16 * CHUNK_Y_SIZE, 16 * CHUNK_Z_SIZE)) // 4096 chunks, 16777216 blocks
}

fun main(args: Array<String>) = runBlocking {
  // Listen for Shutdown request
  val shutdownRequested = AtomicBoolean(false)
  val gameSpeed = AtomicReference<GameSpeed>(UNLIMITED)
  register(MessageChannel(coroutineContext) { message ->
    if (message is ShutdownRequested) shutdownRequested.set(true)
    if (message is GameSpeedUpdated) gameSpeed.set(message.gameSpeed)
  })

  // Load Mods
  val systemDispatcher = newCachedThreadPool(daemonThreadFactory("systems")).asCoroutineDispatcher()
  val modLoaders = ServiceLoader.load(ModLoader::class.java)
  modLoaders.forEach { modLoader -> modLoader.initialize(coroutineContext + systemDispatcher) }
  modLoaders.forEach { modLoader -> modLoader.start() }

  // Run Main Render Loop
  val renderDispatcher = newSingleThreadExecutor(daemonThreadFactory("render")).asCoroutineDispatcher()
  val renderGameSystem = RenderGameSystem(coroutineContext, coroutineContext + renderDispatcher)
  register(renderGameSystem.channel)

  val renderJob = launch(coroutineContext + renderDispatcher) {
    renderGameSystem.glInitialize()
    while (!shutdownRequested.get()) {
      renderGameSystem.glUpdate()
      renderGameSystem.glRender()
      yield()
    }
    renderGameSystem.glShutdown()
  }

  // Run Main Game Loop
  val gameLoopDispatcher = newSingleThreadExecutor(daemonThreadFactory("gameLoop")).asCoroutineDispatcher()
  val updateJob = launch(coroutineContext + gameLoopDispatcher) {
    send(Initialize(Cause.of(this)))
    waitForIdle()

    send(NewGameRequested(GameSize.NORMAL.dimension, 0, Cause.of(this)))
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

private fun daemonThreadFactory(name: String): ThreadFactory {
  val threadNumber = AtomicInteger(1)
  return ThreadFactory { target ->
    val thread = Thread(Thread.currentThread().threadGroup, target, "$name-${threadNumber.getAndIncrement()}")
    thread.isDaemon = false
    thread
  }
}
