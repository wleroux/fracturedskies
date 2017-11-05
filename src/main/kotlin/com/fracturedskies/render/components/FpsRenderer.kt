package com.fracturedskies.render.components

import com.fracturedskies.engine.Render
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.messages.MessageBus.subscribe
import com.fracturedskies.engine.messages.MessageBus.unsubscribe
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.render.components.TextRenderer.Companion.textRenderer
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.EmptyCoroutineContext

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

  lateinit var fpsCounter: MessageChannel
  override fun willMount() {
    super.willMount()
    var last = System.nanoTime()
    var ticks = 0
    fpsCounter = subscribe(MessageChannel(EmptyCoroutineContext, { message ->
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
    }))
  }
  override fun willUnmount() {
    super.willUnmount()
    unsubscribe(fpsCounter)
  }

  override fun componentFromPoint(point: Point): Component<*>? = null

  override fun preferredWidth(parentWidth: Int, parentHeight: Int) =
          children.map({it -> it.preferredWidth(parentWidth, parentHeight)}).max() ?: 0
  override fun preferredHeight(parentWidth: Int, parentHeight: Int) =
          children.map({it -> it.preferredHeight(parentWidth, parentHeight)}).max() ?: 0
  override fun toNode(): List<Node<*>> {
    return nodes {
      textRenderer("FPS: $fps")
    }
  }
}