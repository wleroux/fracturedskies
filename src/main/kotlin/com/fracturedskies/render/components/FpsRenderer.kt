package com.fracturedskies.render.components

import com.fracturedskies.engine.Render
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.unregister
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.render.components.TextRenderer.Companion.textRenderer
import java.util.concurrent.TimeUnit

class FpsRenderer(attributes: Context) : AbstractComponent<Int>(attributes, 0) {
  companion object {
    private val ONE_SECOND_IN_NANOSECONDS = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)
    fun Node.Builder<*>.fpsRenderer() {
      nodes.add(Node(::FpsRenderer))
    }
  }
  /* State */
  var fps
    get() = nextState ?: state
    set(value) { nextState = value }

  lateinit private var fpsCounter: MessageChannel
  override fun willMount() {
    super.willMount()
    var last = System.nanoTime()
    var ticks = 0
    fpsCounter = register(MessageChannel { message ->
      if (message is Render) {
        val now = System.nanoTime()
        if (now - last >= ONE_SECOND_IN_NANOSECONDS) {
          fps = ticks
          ticks = 0
          last = now
        } else {
          ticks++
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
    textRenderer("FPS: $fps")
  }
}