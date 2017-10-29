package com.fracturedskies.render

import com.fracturedskies.engine.GameSystem
import com.fracturedskies.engine.Render
import com.fracturedskies.engine.events.Event
import kotlinx.coroutines.experimental.DefaultDispatcher
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

class FramePerSecondGameSystem(coroutineContext: CoroutineContext = DefaultDispatcher): GameSystem(coroutineContext) {
  companion object {
    private val ONE_SECOND_IN_NANOSECONDS = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)
  }

  private var last = System.nanoTime()
  private var ticks = 0
  override suspend fun invoke(event: Event) {
    if (event !is Render) return

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