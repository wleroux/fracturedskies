package com.fracturedskies.game.render.components.input

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.event.EventHandlers
import com.fracturedskies.engine.jeact.event.on
import com.fracturedskies.engine.jeact.nodes
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.game.render.components.BorderImage.Companion.borderImage
import com.fracturedskies.game.render.components.TextRenderer
import com.fracturedskies.game.render.components.TextRenderer.Companion.textRenderer
import com.fracturedskies.game.render.components.layout.Direction
import com.fracturedskies.game.render.components.layout.Layout.Companion.layout
import com.fracturedskies.game.render.events.CharMods
import com.fracturedskies.game.render.events.Focus
import com.fracturedskies.game.render.events.Unfocus
import com.fracturedskies.game.render.shaders.Material
import com.fracturedskies.game.render.shaders.TextureArray
import com.fracturedskies.game.render.shaders.standard.StandardShaderProgram
import org.lwjgl.glfw.GLFW

class Input(attributes: Context) : AbstractComponent<Input.InputState>(attributes, InputState(attributes[INITIAL_VALUE] ?: "")) {
  companion object {
    val INITIAL_VALUE = Key<String>("initialValue")
    val ON_TEXT_CHANGED = Key<(String) -> Unit>("onTextChanged")
    fun Node.Builder<*>.input(initialValue: String = "", onTextChanged: (String) -> Unit = {}, additionalContext: Context = Context()) {
      nodes.add(Node(::Input, Context(
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
    get() = requireNotNull(attributes[ON_TEXT_CHANGED])

  lateinit private var material: Material
  override fun willMount() {
    material = Material(StandardShaderProgram(), Context(
        StandardShaderProgram.ALBEDO to TextureArray(4, 4, 9, loadByteBuffer("com/fracturedskies/render/components/input/input_default.png", this@Input.javaClass))
    ))
  }

  override fun didMount() {
    onTextChanged(text)
  }

  override fun toNode() = nodes {
    layout(direction = com.fracturedskies.game.render.components.layout.Direction.COLUMN_REVERSE) {
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

  private fun doOnKey(event: com.fracturedskies.game.render.events.Key) {
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

  private fun doOnCursorPosition(event: com.fracturedskies.game.render.components.TextRenderer.CursorPosition) {
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
          on(com.fracturedskies.game.render.events.Key::class) { doOnKey(it) },
          on(CharMods::class) { doOnChar(it) },
          on(com.fracturedskies.game.render.components.TextRenderer.CursorPosition::class) { doOnCursorPosition(it) }
  )
}