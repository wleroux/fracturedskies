package com.fracturedskies.render

import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.unregister
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.render.FpsRenderer.FpsRendererState
import com.fracturedskies.render.common.components.TextRenderer.Companion.text
import java.util.concurrent.TimeUnit

class FpsRenderer(props: MultiTypeMap) : Component<FpsRendererState>(props, FpsRendererState()) {
  companion object {
    private val ONE_SECOND_IN_NANOSECONDS = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)
    fun Node.Builder<*>.fpsRenderer() {
      nodes.add(Node(::FpsRenderer))
    }
  }

  /* State */
  data class FpsRendererState(val fps: Int = 0, val ups: Int = 0)
  private var fps
    get() = (nextState ?: state).fps
    set(value) { nextState = (nextState ?: state).copy(fps = value) }
  private var ups
    get() = (nextState ?: state).ups
    set(value) { nextState = (nextState ?: state).copy(ups = value) }

  private lateinit var fpsCounter: MessageChannel
  override fun componentWillMount() {
    super.componentWillMount()
    fpsCounter = register(MessageChannel { message ->
      if (message is Update) {
        upsTicks++
      }
    })
  }
  override fun componentWillUnmount() {
    super.componentWillUnmount()
    unregister(fpsCounter)
  }
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