package com.fracturedskies.render.components

import com.fracturedskies.engine.api.Render
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.unregister
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.render.components.TextRenderer.Companion.textRenderer
import java.util.concurrent.TimeUnit

class FpsRenderer(attributes: MultiTypeMap) : AbstractComponent<FpsRenderer.FpsData>(attributes, FpsData(0, 0)) {
  companion object {
    private val ONE_SECOND_IN_NANOSECONDS = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)
    fun Node.Builder<*>.fpsRenderer() {
      nodes.add(Node(::FpsRenderer))
    }
  }
  /* State */
  data class FpsData(val fps: Int, val ups: Int)
  var fps
    get() = nextState?.fps ?: state.fps
    set(value) { nextState = FpsData(value, ups) }
  var ups
    get() = nextState?.ups ?: state.ups
    set(value) { nextState = FpsData(fps, value) }

  lateinit private var fpsCounter: MessageChannel
  override fun willMount() {
    super.willMount()
    var lastFps = System.nanoTime()
    var fpsTicks = 0

    var lastUps = System.nanoTime()
    var upsTicks = 0
    fpsCounter = register(MessageChannel { message ->
      when (message) {
        is Render -> {
          val now = System.nanoTime()
          if (now - lastFps >= ONE_SECOND_IN_NANOSECONDS) {
            fps = fpsTicks
            fpsTicks = 0
            lastFps = now
          } else {
            fpsTicks++
          }
        }
        is Update -> {
          val now = System.nanoTime()
          if (now - lastUps >= ONE_SECOND_IN_NANOSECONDS) {
            ups = upsTicks
            upsTicks = 0
            lastUps = now
          } else {
            upsTicks++
          }
        }
      }
    })
  }
  override fun willUnmount() {
    super.willUnmount()
    unregister(fpsCounter)
  }
  override fun componentFromPoint(point: Point): Component<*>? = null
  override fun toNode() = nodes {
    textRenderer("FPS: $fps, MSPF: ${1000f / fps}; UPS: $ups, MSPF: ${1000f / ups}")
  }
}