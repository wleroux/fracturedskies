package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.Color4
import com.fracturedskies.engine.messages.MessageBus.subscribe
import com.fracturedskies.engine.messages.MessageBus.unsubscribe
import com.fracturedskies.render.components.TextRenderer.Companion.COLOR
import com.fracturedskies.render.components.TextRenderer.Companion.TEXT
import com.fracturedskies.render.mesh.text.Text
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield

class FpsRenderer(attributes: Context) : AbstractComponent<Int>(attributes, 0) {
  var fps
    get() = nextState ?: state
    set(value) { nextState = value }

  lateinit var fpsCounter: FramePerSecondGameSystem
  override fun willMount() {
    super.willMount()
    fpsCounter = FramePerSecondGameSystem()
    subscribe(fpsCounter)
    launch {
      while (isActive) {
        fps = fpsCounter.fps
        yield()
      }
    }
  }

  override fun willUnmount() {
    super.willUnmount()
    unsubscribe(fpsCounter)
  }

  override fun componentFromPoint(point: Point): Component<*>? = null

  override fun toNode(): List<Node<*>> {
    return listOf(Node(::TextRenderer, Context(
            TEXT to Text("FPS: $fps"),
            COLOR to Color4.WHITE
    )))
  }

  override fun render(bounds: Bounds) {
    this.bounds = bounds
    for (child in children) {
      child.render(this.bounds)
    }
  }
}