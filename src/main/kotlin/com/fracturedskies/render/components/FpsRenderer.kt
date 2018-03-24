package com.fracturedskies.render.components

import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.unregister
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.render.components.FpsRenderer.FpsRendererState
import com.fracturedskies.render.components.TextRenderer.Companion.textRenderer
import java.util.concurrent.TimeUnit

class FpsRenderer(attributes: MultiTypeMap) : Component<FpsRendererState>(attributes, FpsRendererState()) {
  companion object {
    private val ONE_SECOND_IN_NANOSECONDS = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)
    fun Node.Builder<*>.fpsRenderer() {
      nodes.add(Node(::FpsRenderer))
    }
  }

  /* State */
  data class FpsRendererState(val fps: Int = 0, val ups: Int = 0)
  var fps
    get() = (nextState ?: state).fps
    set(value) { nextState = (nextState ?: state).copy(fps = value) }
  var ups
    get() = (nextState ?: state).ups
    set(value) { nextState = (nextState ?: state).copy(ups = value) }

  private lateinit var fpsCounter: MessageChannel
  override fun willMount() {
    super.willMount()
    var lastUps = System.nanoTime()
    var upsTicks = 0
    fpsCounter = register(MessageChannel { message ->
      if (message is Update) {
        // Track UPS ticks
        val now = System.nanoTime()
        if (now - lastUps >= ONE_SECOND_IN_NANOSECONDS) {
          ups = upsTicks
          upsTicks = 0
          lastUps = now
        } else {
          upsTicks++
        }
      }
    })
  }
  override fun willUnmount() {
    super.willUnmount()
    unregister(fpsCounter)
  }
  override fun componentFromPoint(point: Point): Component<*>? = null
  override fun toNodes() = nodes {
    textRenderer("FPS: $fps, MSPF: ${1000f / fps}; UPS: $ups, MSPU: ${1000f / ups}")
  }

  var fpsTicks = 0
  var lastFps = System.nanoTime()

  override fun render(bounds: Bounds) {
    super.render(bounds)

    // Track FPS ticks
    val now = System.nanoTime()
    if (now - lastFps >= ONE_SECOND_IN_NANOSECONDS) {
      fps = fpsTicks
      fpsTicks = 0
      lastFps = now
    } else {
      fpsTicks ++
    }
  }
}