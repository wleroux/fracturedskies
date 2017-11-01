package com.fracturedskies

import com.fracturedskies.engine.*
import com.fracturedskies.engine.events.*
import com.fracturedskies.engine.events.EventBus.publishAndWait
import com.fracturedskies.engine.events.EventBus.subscribe
import com.fracturedskies.engine.jeact.VNode
import com.fracturedskies.render.FramePerSecondGameSystem
import com.fracturedskies.render.LoggingGameSystem
import com.fracturedskies.render.RenderGameSystem
import com.fracturedskies.render.components.AlternatingBlock
import kotlinx.coroutines.experimental.*
import java.util.concurrent.TimeUnit.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.CoroutineContext

class MainGameSystem(coroutineContext: CoroutineContext): GameSystem(coroutineContext) {
  companion object {
    private val MILLISECONDS_PER_UPDATE: Long = 50
    private val NANOSECONDS_PER_UPDATE: Long = NANOSECONDS.convert(MILLISECONDS_PER_UPDATE, MILLISECONDS)
  }

  private val shutdownRequested = AtomicBoolean(false)

  suspend fun run(coroutineContext: CoroutineContext) {
    publishAndWait(Initialize(Cause.of(this), Context.empty()))
    var last = System.nanoTime()
    while (!shutdownRequested.get()) {
      val now = System.nanoTime()
      while (now - last >= NANOSECONDS_PER_UPDATE) {
        publishAndWait(Update(MILLISECONDS_PER_UPDATE, Cause.of(this), Context.empty()))
        last += NANOSECONDS_PER_UPDATE
      }

      val alpha = (now - last).toFloat() / NANOSECONDS_PER_UPDATE.toFloat()
      publishAndWait(Render(alpha, Cause.of(this), Context.empty()))
    }

    publishAndWait(Shutdown(Cause.of(this), Context.empty()))
    coroutineContext.cancelChildren()
  }

  suspend override fun invoke(event: Event) {
    when (event) {
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
  subscribe(RenderGameSystem(coroutineContext, VNode(::AlternatingBlock)))

  // Run game
  mainGameSystem.run(coroutineContext)
}
