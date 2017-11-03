package com.fracturedskies

import com.fracturedskies.engine.*
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.Message
import com.fracturedskies.engine.messages.MessageBus.publishAndWait
import com.fracturedskies.engine.messages.MessageBus.subscribe
import com.fracturedskies.render.components.FramePerSecondGameSystem
import com.fracturedskies.render.LoggingGameSystem
import com.fracturedskies.render.RenderGameSystem
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.cancelChildren
import kotlinx.coroutines.experimental.runBlocking
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.CoroutineContext

class MainGameSystem(coroutineContext: CoroutineContext): GameSystem(coroutineContext) {
  companion object {
    private val MILLISECONDS_PER_UPDATE: Long = 50
    private val NANOSECONDS_PER_UPDATE: Long = NANOSECONDS.convert(MILLISECONDS_PER_UPDATE, MILLISECONDS)
  }

  private val shutdownRequested = AtomicBoolean(false)

  suspend fun run(coroutineContext: CoroutineContext) {
    publishAndWait(Initialize(Cause.of(this), Context()))
    var last = System.nanoTime()
    while (!shutdownRequested.get()) {
      val now = System.nanoTime()
      while (now - last >= NANOSECONDS_PER_UPDATE) {
        publishAndWait(Update(MILLISECONDS_PER_UPDATE, Cause.of(this), Context()))
        last += NANOSECONDS_PER_UPDATE
      }

      val alpha = (now - last).toFloat() / NANOSECONDS_PER_UPDATE.toFloat()
      publishAndWait(Render(alpha, Cause.of(this), Context()))
    }

    publishAndWait(Shutdown(Cause.of(this), Context()))
    coroutineContext.cancelChildren()
  }

  suspend override fun invoke(message: Message) {
    when (message) {
      is RequestShutdown -> shutdownRequested.set(true)
    }
  }
}

fun main(args: Array<String>) = runBlocking<Unit> {
  // Subscribe all game systems
  val mainGameSystem = MainGameSystem(coroutineContext)
  subscribe(mainGameSystem)
  subscribe(LoggingGameSystem(coroutineContext + CommonPool))
  subscribe(FramePerSecondGameSystem(coroutineContext + CommonPool))
  subscribe(RenderGameSystem(coroutineContext))

  // Run game
  mainGameSystem.run(coroutineContext)
}
