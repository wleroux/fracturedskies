package com.fracturedskies.render.common.components.button

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.event.*
import com.fracturedskies.engine.math.Color4
import com.fracturedskies.render.common.components.button.BaseComponent.Companion.base
import com.fracturedskies.render.common.components.layout.*
import com.fracturedskies.render.common.components.layout.Layout.Companion.layout
import com.fracturedskies.render.common.events.*
import com.fracturedskies.render.common.style.*
import org.lwjgl.glfw.GLFW

enum class ButtonMode {
  DEFAULT {
    override fun color(): Color4 = Color4(141, 168, 120, 255)
  },
  HOVER {
    override fun color(): Color4 = Color4(146, 185, 116, 255)
  },
  PRESSED {
    override fun color(): Color4 = Color4(77, 105, 56, 255)
  },
  FOCUS {
    override fun color(): Color4 = Color4(124, 148, 106, 255)
  },
  DISABLED {
    override fun color(): Color4 = Color4(189, 198, 182, 255)
  };

  abstract fun color(): Color4
}

data class ButtonState(
    val pressed: Boolean = false,
    val hover: Boolean = false,
    val focus: Boolean = false
)

class Button(props: MultiTypeMap) : Component<ButtonState>(props, ButtonState()) {
  companion object {
    private val COLOR = TypedKey<Color4>("color")
    private val PADDING = TypedKey<Padding>("padding")
    private val MARGIN = TypedKey<Margin>("margin")
    private val BORDER = TypedKey<Border>("border")
    private val DISABLED = TypedKey<Boolean>("disabled")
    private val ON_CLICK = TypedKey<(Click) -> Unit>("onClick")
    fun Node.Builder<*>.button(
        color: Color4 = Color4.WHITE,
        padding: Padding = Padding(),
        margin: Margin = Margin(),
        border: Border = Border(),
        disabled: Boolean = false,
        onClick: (Click) -> Unit = {},
        additionalContext: MultiTypeMap = MultiTypeMap(),
        block: Node.Builder<*>.()->Unit = {}
    ) {
      nodes.add(Node(::Button, MultiTypeMap(
          COLOR to color,
          PADDING to padding,
          MARGIN to margin,
          BORDER to border,
          DISABLED to disabled,
          ON_CLICK to onClick
      ).with(additionalContext), block))
    }
  }

  override fun render() = nodes {
    val mode = when {
      props[DISABLED] -> ButtonMode.DISABLED
      state.pressed -> ButtonMode.PRESSED
      state.hover -> ButtonMode.HOVER
      state.focus -> ButtonMode.FOCUS
      else -> ButtonMode.DEFAULT
    }

    base(
        color = mode.color(),
        margin = props[MARGIN],
        padding = props[PADDING],
        border = props[BORDER]
    ) {
      layout(alignContent = ContentAlign.STRETCH, alignItems = ItemAlign.CENTER, justifyContent = JustifyContent.CENTER) {
        nodes.addAll(super.render())
      }
    }
  }

  @Suppress("UNUSED_PARAMETER")
  private fun doOnFocus(event: Focus) {
    if (!props[DISABLED]) {
      nextState = currentState.copy(focus = true)
    }
  }

  @Suppress("UNUSED_PARAMETER")
  private fun doOnUnfocus(event: Unfocus) {
    if (!props[DISABLED]) {
      nextState = currentState.copy(focus = false)
    }
  }

  private fun doOnClick(event: Click) {
    if (!props[DISABLED]) {
      if (event.action == GLFW.GLFW_PRESS)
        nextState = currentState.copy(pressed = true)
      if (event.action == GLFW.GLFW_RELEASE) {
        props[ON_CLICK].invoke(event)
        nextState = currentState.copy(pressed = false)
      }
    }
  }
  @Suppress("UNUSED_PARAMETER")
  private fun doOnHover(event: Hover) {
    if (!props[DISABLED]) {
      nextState = currentState.copy(hover = true)
    }
  }
  @Suppress("UNUSED_PARAMETER")
  private fun doOnUnhover(event: Unhover) {
    if (!props[DISABLED]) {
      nextState = currentState.copy(pressed = false, hover = false)
    }
  }

  override val handler = EventHandlers(
      on(Focus::class) { event -> doOnFocus(event) },
      on(Unfocus::class) { event -> doOnUnfocus(event) },
      on(Hover::class) { event -> doOnHover(event) },
      on(Unhover::class) { event -> doOnUnhover(event) },
      on(Click::class) { event -> doOnClick(event)}
  )
}