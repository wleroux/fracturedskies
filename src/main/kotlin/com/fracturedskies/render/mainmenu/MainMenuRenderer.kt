package com.fracturedskies.render.mainmenu

import com.fracturedskies.GameSize
import com.fracturedskies.GameSize.NORMAL
import com.fracturedskies.api.NewGameRequested
import com.fracturedskies.engine.api.ShutdownRequested
import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.Color4
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.render.common.components.TextRenderer.Companion.text
import com.fracturedskies.render.common.components.button.Button.Companion.button
import com.fracturedskies.render.common.components.layout.*
import com.fracturedskies.render.common.components.layout.ContentAlign.STRETCH
import com.fracturedskies.render.common.components.layout.Direction.COLUMN_REVERSE
import com.fracturedskies.render.common.components.layout.JustifyContent.CENTER
import com.fracturedskies.render.common.components.layout.Layout.Companion.layout
import com.fracturedskies.render.common.components.layout.Wrap.NO_WRAP
import com.fracturedskies.render.common.style.*
import com.fracturedskies.render.common.style.Border.*
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
    val defaultMargin = Margin(4, 0, 4, 0)
    val defaultPadding = Padding(4, 8, 4, 8)
    val defaultBorder = Border(
        color = Color4.WHITE,
        width = BorderWidth(1, 1, 1, 1),
        radius = BorderRadius(8, 8, 8, 8)
    )


    if (state.mode == MAIN_MENU) {
      layout(justifyContent = CENTER, alignContent = ContentAlign.CENTER) {
        layout(COLUMN_REVERSE, CENTER, ItemAlign.STRETCH, ContentAlign.STRETCH, NO_WRAP) {
          button(
              margin = defaultMargin,
              padding = defaultPadding,
              border = defaultBorder,
              onClick = { _ -> this@MainMenuRenderer.setMode(NEW_GAME) }
          ) { text("New Game") }
          button(
              margin = defaultMargin,
              padding = defaultPadding,
              border = defaultBorder,
              onClick = { _ -> this@MainMenuRenderer.onQuit() }
          ) { text("Quit Game")}
        }
      }
    } else {
      layout(COLUMN_REVERSE, alignItems = ItemAlign.STRETCH, alignContent = ContentAlign.CENTER, justifyContent = CENTER, wrap = NO_WRAP) {
        layout(alignItems = ItemAlign.CENTER, alignContent = STRETCH) {
          text("Game Size: ${state.gameSize.name}")
        }

        layout(COLUMN_REVERSE, alignItems = ItemAlign.STRETCH, alignContent = ContentAlign.CENTER, justifyContent = CENTER, wrap = NO_WRAP) {
          // Button Group
          GameSize.values().forEachIndexed { index, gameSize ->
            val isTopButton = index == 0
            val isBottomButton = index == GameSize.values().size - 1
            val margin = defaultMargin.copy(
                top = if (isTopButton) defaultMargin.top else 0,
                bottom = if (isBottomButton) defaultMargin.bottom else 0
            )
            val border = defaultBorder.copy(
                width = defaultBorder.width.copy(
                    bottom = if (isBottomButton) defaultBorder.width.bottom else 0
                ),
                radius =  defaultBorder.radius.copy(
                  topLeft = if (isTopButton) defaultBorder.radius.topLeft else 0,
                  topRight = if (isTopButton) defaultBorder.radius.topRight else 0,
                  bottomLeft = if (isBottomButton) defaultBorder.radius.bottomLeft else 0,
                  bottomRight = if (isBottomButton) defaultBorder.radius.bottomRight else 0
                )
            )

            button(
                margin = margin,
                padding = defaultPadding,
                border = border,
                onClick = { _ -> this@MainMenuRenderer.onSelectGameSize(gameSize) }
            ) { text(gameSize.name) }
          }

          button(
              margin = defaultMargin,
              padding = defaultPadding,
              border = defaultBorder,
              onClick = { _ -> this@MainMenuRenderer.startGame() }
          ) { text("Start Game") }

          button(
              margin = defaultMargin,
              padding = defaultPadding,
              border = defaultBorder,
              onClick = { _ -> this@MainMenuRenderer.setMode(MAIN_MENU) }
          ) { text("Back") }
        }
      }
    }
  }
}