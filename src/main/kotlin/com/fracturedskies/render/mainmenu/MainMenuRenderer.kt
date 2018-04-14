package com.fracturedskies.render.mainmenu

import com.fracturedskies.GameSize
import com.fracturedskies.GameSize.NORMAL
import com.fracturedskies.api.NewGameRequested
import com.fracturedskies.engine.api.ShutdownRequested
import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.render.common.components.TextRenderer.Companion.text
import com.fracturedskies.render.common.components.button.Button.Companion.button
import com.fracturedskies.render.common.components.gl.GLViewport.Companion.viewport
import com.fracturedskies.render.common.components.gl.GLViewport.Padding
import com.fracturedskies.render.common.components.layout.*
import com.fracturedskies.render.common.components.layout.Direction.COLUMN_REVERSE
import com.fracturedskies.render.common.components.layout.JustifyContent.CENTER
import com.fracturedskies.render.common.components.layout.Layout.Companion.layout
import com.fracturedskies.render.common.components.layout.Wrap.NO_WRAP
import com.fracturedskies.render.mainmenu.MainMenuRenderer.MenuState
import com.fracturedskies.render.mainmenu.MainMenuRenderer.Mode.*


class MainMenuRenderer(props: MultiTypeMap) : Component<MenuState>(props, MenuState()) {
  companion object {
    fun Node.Builder<*>.mainMenu(additionalContext: MultiTypeMap = MultiTypeMap(), block: Node.Builder<*>.() -> Unit = {}) {
      nodes.add(Node(::MainMenuRenderer, additionalContext, block))
    }
  }

  data class MenuState(
      val mode: Mode = MAIN_MENU,
      val gameSize: GameSize = NORMAL
  )
  enum class Mode {
    MAIN_MENU,
    NEW_GAME
  }

  private fun setMode(mode: Mode) {
    nextState = currentState.copy(mode = mode)
  }

  private fun onSelectGameSize(gameSize: GameSize) {
    nextState = currentState.copy(gameSize = gameSize)
  }

  private fun onQuit() {
    send(ShutdownRequested(Cause.of(this)))
  }

  private fun startGame() {
    send(NewGameRequested(currentState.gameSize.dimension, 0, Cause.of(this)))
  }

  override fun render() = nodes {
    if (state.mode == MAIN_MENU) {
      layout(justifyContent = CENTER, alignContent = ContentAlign.CENTER) {
        layout(COLUMN_REVERSE, CENTER, ItemAlign.STRETCH, ContentAlign.STRETCH, NO_WRAP) {
          viewport(Padding(10, 10, 10, 10)) {
            button(onClick = { _ -> this@MainMenuRenderer.setMode(NEW_GAME) }) { text("New Game") }
          }
          viewport(Padding(10, 10, 10, 10)) {
            button(onClick = { _ -> this@MainMenuRenderer.onQuit() }) { text("Quit Game") }
          }
        }
      }
    } else {
      layout(justifyContent = CENTER, alignContent = ContentAlign.CENTER) {
        layout(COLUMN_REVERSE, CENTER, ItemAlign.STRETCH, ContentAlign.STRETCH, NO_WRAP) {
          viewport(Padding(5, 0, 5, 0)) {
            layout(COLUMN_REVERSE, CENTER, ItemAlign.STRETCH, ContentAlign.STRETCH, NO_WRAP) {
              text("Game Size: ${state.gameSize.name}")
              for (size in GameSize.values()) {
                viewport(Padding(5, 0, 5, 0)) {
                  button(onClick = { _ -> this@MainMenuRenderer.onSelectGameSize(size) }) { text(size.name) }
                }
              }
            }
          }

          viewport(Padding(5, 0, 5, 0)) {
            button(onClick = { _ -> this@MainMenuRenderer.startGame()}) { text("Start Game") }
          }
          viewport(Padding(5, 0, 5, 0)) {
            button(onClick = { _ -> this@MainMenuRenderer.setMode(MAIN_MENU)}) { text("Back") }
          }
        }
      }
    }
  }
}