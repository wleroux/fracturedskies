package com.fracturedskies.render.common.components.input

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.event.*
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.render.common.components.BorderImage.Companion.borderImage
import com.fracturedskies.render.common.components.TextRenderer
import com.fracturedskies.render.common.components.TextRenderer.Companion.textRenderer
import com.fracturedskies.render.common.components.input.Input.InputState
import com.fracturedskies.render.common.components.layout.Direction
import com.fracturedskies.render.common.components.layout.Layout.Companion.layout
import com.fracturedskies.render.common.events.*
import com.fracturedskies.render.common.shaders.*
import com.fracturedskies.render.common.shaders.standard.StandardShaderProgram
import org.lwjgl.glfw.GLFW

class Input(props: MultiTypeMap) : Component<InputState>(props, InputState(props[INITIAL_VALUE]
    ?: "")) {
  companion object {
    val INITIAL_VALUE = TypedKey<String>("initialValue")
    val ON_TEXT_CHANGED = TypedKey<(String) -> Unit>("onTextChanged")
    fun Node.Builder<*>.input(initialValue: String = "", onTextChanged: (String) -> Unit = {}, additionalContext: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::Input, MultiTypeMap(
              INITIAL_VALUE to initialValue,
              ON_TEXT_CHANGED to onTextChanged
      ).with(additionalContext)))
    }
  }

  /* State */
  private var text: String
    get() = nextState?.text ?: state.text
    set(value) {nextState = (nextState?:state).copy(text = value)}
  private var cursorPosition: Int
    get() = nextState?.cursorPosition ?: state.cursorPosition
    set(value) {nextState = (nextState?:state).copy(cursorPosition = value)}
  private var focused: Boolean
    get() = nextState?.focused ?: state.focused
    set(value) {nextState = (nextState?:state).copy(focused = value)}

  /* Attributes */
  private val onTextChanged
    get() = requireNotNull(props[ON_TEXT_CHANGED])

  private lateinit var material: Material
  override fun componentWillMount() {
    material = Material(StandardShaderProgram(), MultiTypeMap(
        StandardShaderProgram.ALBEDO to TextureArray(4, 4, 9, loadByteBuffer("com/fracturedskies/render/components/input/input_default.png", this@Input.javaClass))
    ))
  }

  override fun componentDidMount() {
    onTextChanged(text)
  }

  override fun render() = nodes {
    layout(direction = Direction.COLUMN_REVERSE) {
      borderImage(material) {
        val displayText = if (focused) {
          text.substring(0, cursorPosition) + "_" + text.substring(cursorPosition)
        } else {
          text
        }
        textRenderer(displayText)
      }
    }
  }

  data class InputState(val text: String = "Hello, World!", val cursorPosition: Int = 0, val focused: Boolean = false)

  private fun doOnFocus(event: Focus) {
    focused = true
    event.stopPropogation = true
  }
  private fun doOnUnfocus(event: Unfocus) {
    focused = false
    event.stopPropogation = true
  }

  private fun doOnKey(event: Key) {
    if (event.action == GLFW.GLFW_PRESS || event.action == GLFW.GLFW_REPEAT) {
      when (event.key) {
        GLFW.GLFW_KEY_BACKSPACE -> {
          if (cursorPosition != 0) {
            text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition)
            cursorPosition--
            onTextChanged(text)
          }
        }
        GLFW.GLFW_KEY_DELETE -> {
          if (cursorPosition != text.length) {
            text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1)
            onTextChanged(text)
          }
        }
        GLFW.GLFW_KEY_LEFT -> {
          if (cursorPosition != 0) {
            cursorPosition --
            if (event.mods and GLFW.GLFW_MOD_CONTROL != 0) {
              while (cursorPosition != 0) {
                if (text.substring(cursorPosition - 1, cursorPosition) == " ") {
                  break
                }
                cursorPosition --
              }
            }
          }
        }
        GLFW.GLFW_KEY_RIGHT -> {
          if (cursorPosition != text.length) {
            cursorPosition++
            if (event.mods and GLFW.GLFW_MOD_CONTROL != 0) {
              while (cursorPosition != text.length) {
                if (text.substring(cursorPosition - 1, cursorPosition) == " ") {
                  break
                }
                cursorPosition ++
              }
            }
          }
        }
      }
    }
  }

  private fun doOnCursorPosition(event: TextRenderer.CursorPosition) {
    if (focused) {
      val selectedCursorPosition = if (event.cursorPosition >= cursorPosition) event.cursorPosition - 1 else event.cursorPosition
      cursorPosition = maxOf(0, minOf(selectedCursorPosition, text.length))
    }
  }

  private fun doOnChar(event: CharMods) {
    val character = String(java.lang.Character.toChars(event.codepoint))
    text = text.substring(0, cursorPosition) + character + text.substring(cursorPosition)
    cursorPosition ++
    onTextChanged(text)
  }

  override val handler = EventHandlers(
          on(Focus::class) { doOnFocus(it) },
          on(Unfocus::class) { doOnUnfocus(it) },
          on(Key::class) { doOnKey(it) },
          on(CharMods::class) { doOnChar(it) },
          on(TextRenderer.CursorPosition::class) { doOnCursorPosition(it) }
  )
}