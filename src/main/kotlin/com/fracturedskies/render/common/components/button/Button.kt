package com.fracturedskies.render.common.components.button

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.event.*
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.render.common.components.BorderImage.Companion.borderImage
import com.fracturedskies.render.common.events.*
import com.fracturedskies.render.common.shaders.*
import com.fracturedskies.render.common.shaders.standard.StandardShaderProgram
import org.lwjgl.glfw.GLFW

class Button(props: MultiTypeMap) : Component<Boolean>(props, false) {
  companion object {
    val ON_CLICK = TypedKey<(Click) -> Unit>("onClick")
    fun Node.Builder<*>.button(onClick: (Click) -> Unit = {}, additionalContext: MultiTypeMap = MultiTypeMap(), block: Node.Builder<*>.()->Unit = {}) {
      nodes.add(Node(::Button, MultiTypeMap(
              ON_CLICK to onClick
      ).with(additionalContext), block))
    }
  }

  /* State */
  private var hover: Boolean
    get() = nextState ?: state
    set(value) {nextState = value}

  /* Attributes */
  private val onClick get() = requireNotNull(props[ON_CLICK])

  private lateinit var defaultMaterial: Material
  private lateinit var hoverMaterial: Material
  override fun componentWillMount() {
    hoverMaterial = Material(StandardShaderProgram(), MultiTypeMap(
        StandardShaderProgram.ALBEDO to TextureArray(4, 4, 9, loadByteBuffer("button_hover.png", this@Button.javaClass))
    ))
    defaultMaterial = Material(StandardShaderProgram(), MultiTypeMap(
        StandardShaderProgram.ALBEDO to TextureArray(4, 4, 9, loadByteBuffer("button_default.png", this@Button.javaClass))
    ))
  }

  override fun render() = nodes {
    borderImage(if (hover) hoverMaterial else defaultMaterial) {
      nodes.addAll(super.render())
    }
  }

  private fun doOnClick(event: Click) {
    if (event.action == GLFW.GLFW_RELEASE)
      onClick(event)
  }
  private fun doOnHover(event: Hover) {
    hover = true
    event.stopPropogation = true
  }
  private fun doOnUnhover(event: Unhover) {
    hover = false
    event.stopPropogation = true
  }

  override val handler = EventHandlers(
          on(Hover::class) { event -> doOnHover(event) },
          on(Unhover::class) { event -> doOnUnhover(event) },
          on(Click::class) { event -> doOnClick(event)}
  )
}