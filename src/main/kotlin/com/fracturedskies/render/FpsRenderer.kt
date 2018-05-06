package com.fracturedskies.render

import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.render.FpsRenderer.FpsRendererState
import com.fracturedskies.render.common.components.TextRenderer.Companion.text
import java.util.concurrent.TimeUnit
import javax.enterprise.event.Observes

class FpsRenderer : Component<FpsRendererState>(FpsRendererState()) {
  companion object {
    private val ONE_SECOND_IN_NANOSECONDS = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)
    fun (Node.Builder<*>).fpsRenderer() {
      nodes.add(Node(FpsRenderer::class))
    }
  }

  fun onUpdate(@Observes update: Update) {
    upsTicks ++
  }

  /* State */
  data class FpsRendererState(val fps: Int = 0, val ups: Int = 0)
  private var fps
    get() = (nextState ?: state).fps
    set(value) { nextState = (nextState ?: state).copy(fps = value) }
  private var ups
    get() = (nextState ?: state).ups
    set(value) { nextState = (nextState ?: state).copy(ups = value) }

  override fun glComponentFromPoint(point: Point): Component<*>? = null
  override fun render() = nodes {
    text("FPS: $fps, UPS: $ups")
  }

  var upsTicks = 0
  var fpsTicks = 0
  var lastUpdate = System.nanoTime()

  override fun glRender(bounds: Bounds) {
    super.glRender(bounds)

    // Track FPS ticks
    val now = System.nanoTime()
    if (now - lastUpdate >= ONE_SECOND_IN_NANOSECONDS) {
      fps = fpsTicks
      ups = upsTicks
      fpsTicks = 0
      upsTicks = 0
      lastUpdate = now
    } else {
      fpsTicks ++
    }
  }
}