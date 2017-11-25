package com.fracturedskies.render.components.button

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.event.EventHandlers
import com.fracturedskies.engine.jeact.event.on
import com.fracturedskies.engine.jeact.nodes
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.render.components.BorderImage.Companion.borderImage
import com.fracturedskies.render.events.Click
import com.fracturedskies.render.events.Hover
import com.fracturedskies.render.events.Unhover
import com.fracturedskies.render.shaders.Material
import com.fracturedskies.render.shaders.TextureArray
import com.fracturedskies.render.shaders.standard.StandardShaderProgram
import org.lwjgl.glfw.GLFW

class Button(attributes: Context) : AbstractComponent<Boolean>(attributes, false) {
  companion object {
    val ON_CLICK = Key<(Click) -> Unit>("onClick")
    fun Node.Builder<*>.button(onClick: (Click) -> Unit = {}, additionalContext: Context = Context(), block: Node.Builder<*>.()->Unit = {}) {
      nodes.add(Node(::Button, Context(
              ON_CLICK to onClick
      ).with(additionalContext), block))
    }
  }

  /* State */
  private var hover: Boolean
    get() = nextState ?: state
    set(value) {nextState = value}

  /* Attributes */
  private val onClick get() = requireNotNull(attributes[ON_CLICK])

  lateinit private var defaultMaterial: Material
  lateinit private var hoverMaterial: Material
  override fun willMount() {
    hoverMaterial = Material(StandardShaderProgram(), Context(
            StandardShaderProgram.ALBEDO to TextureArray("button_hover.png", loadByteBuffer("button_hover.png", this@Button.javaClass), 4, 4, 9)
    ))
    defaultMaterial = Material(StandardShaderProgram(), Context(
            StandardShaderProgram.ALBEDO to TextureArray("button_default.png", loadByteBuffer("button_default.png", this@Button.javaClass), 4, 4, 9)
    ))
  }

  override fun toNode() = nodes {
    borderImage(if (hover) hoverMaterial else defaultMaterial) {
      nodes.addAll(super.toNode())
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
          on(Hover::class) {event -> doOnHover(event) },
          on(Unhover::class) {event -> doOnUnhover(event) },
          on(Click::class) {event -> doOnClick(event)}
  )
}