package com.fracturedskies

import com.fracturedskies.api.World
import com.fracturedskies.engine.api.*
import com.fracturedskies.render.RenderGameSystem
import java.util.concurrent.TimeUnit.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport.parkNanos
import java.util.logging.LogManager
import javax.enterprise.event.*
import javax.enterprise.inject.se.SeContainerInitializer
import javax.inject.*

@Singleton
class Main {

  @Inject
  lateinit var world: World

  private val shutdownRequested = AtomicBoolean(false)
  fun onShutdownRequested(@Observes message: ShutdownRequested) {
    shutdownRequested.set(true)
  }

  @Inject
  private lateinit var events: Event<Message>

  @Inject
  private lateinit var renderGameSystem: RenderGameSystem

  fun run() {
    events.fire(Initialize(Cause.of(this)))
    renderGameSystem.glInitialize()
    var last = System.nanoTime()
    while (!shutdownRequested.get()) {
      if (world.started) {
        val now = System.nanoTime()
        if (now - last >= MILLISECONDS.toNanos(world.gameSpeed.msBetweenUpdates)) {
          events.fire(Update(Cause.of(this)))
          last = now
        } else {
          parkNanos(MICROSECONDS.toNanos(125L))
        }
      }

      renderGameSystem.glUpdate()
      renderGameSystem.glRender()
    }
    renderGameSystem.glShutdown()
    events.fire(Shutdown(Cause.of(this)))
  }
}

fun main(args: Array<String>) {
  // Configure Logger
  Main::class.java.getResourceAsStream("/logging.properties").use { inputStream ->
    LogManager.getLogManager().readConfiguration(inputStream)
  }

  // Start CDI container
  SeContainerInitializer.newInstance().initialize().use { container ->
    container.beanManager.createInstance().select(Main::class.java).get().run()
  }
}
