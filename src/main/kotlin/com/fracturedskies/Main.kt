package com.fracturedskies

import com.fracturedskies.engine.*
import com.fracturedskies.engine.events.*
import com.fracturedskies.engine.events.EventBus.publishAndWait
import com.fracturedskies.engine.events.EventBus.subscribe
import kotlinx.coroutines.experimental.*
import java.util.concurrent.TimeUnit.*
import kotlin.coroutines.experimental.CoroutineContext

fun main(args: Array<String>) = runBlocking<Unit> {
  // Subscribe all game systems
  subscribe(LoggingGameSystem())
  subscribe(FramePerSecondGameSystem())
  subscribe(RenderGameSystem(coroutineContext))

  // Main Game Loop
  publishAndWait(Initialize(Cause.of(this), Context.empty()))
  var last = System.nanoTime()
  while (isActive) {
    val now = System.nanoTime()
    val deltaTimeInMilliseconds = MILLISECONDS.convert(now - last, NANOSECONDS)
    publishAndWait(Update(deltaTimeInMilliseconds, Cause.of(this), Context.empty()))
    last = now
  }
  publishAndWait(Shutdown(Cause.of(this), Context.empty()))
}

class RenderGameSystem(coroutineContext: CoroutineContext = DefaultDispatcher): GameSystem(coroutineContext) {
  override suspend fun invoke(event: Event) {
    delay(16)
  }
}

class LoggingGameSystem(coroutineContext: CoroutineContext = DefaultDispatcher): GameSystem(coroutineContext) {
  override suspend fun invoke(event: Event) {
    println(event)
  }
}

class FramePerSecondGameSystem(coroutineContext: CoroutineContext = DefaultDispatcher): GameSystem(coroutineContext) {
  companion object {
    private val ONE_SECOND_IN_NANOSECONDS = NANOSECONDS.convert(1, SECONDS)
  }

  private var last = System.nanoTime()
  private var ticks = 0
  override suspend fun invoke(event: Event) {
    if (event !is Update) return

    val now = System.nanoTime()
    if (now - last >= ONE_SECOND_IN_NANOSECONDS) {
      println("FPS: $ticks")
      ticks = 0
      last = now
    } else {
      ticks ++
    }
  }
}