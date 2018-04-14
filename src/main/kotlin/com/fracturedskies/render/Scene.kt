package com.fracturedskies.render

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.render.FpsRenderer.Companion.fpsRenderer
import com.fracturedskies.render.common.components.layout.*
import com.fracturedskies.render.common.components.layout.Layout.Companion.GROW
import com.fracturedskies.render.common.components.layout.Layout.Companion.layout
import com.fracturedskies.render.mainmenu.MainMenuRenderer.Companion.mainMenu
import com.fracturedskies.render.world.components.WorldController.Companion.worldController
import org.lwjgl.opengl.GL11.*

class Scene(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    val GAME_STATE = TypedKey<GameState>("gameState")
  }
  private val gameState get() = props[GAME_STATE]

  var prevGameStarted = false
  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean {
    if (super.shouldComponentUpdate(nextProps, nextState)) {
      return true
    } else {
      if (prevGameStarted != gameState.gameStarted) return true
      return false
    }
  }

  override fun componentWillUpdate(nextProps: MultiTypeMap, nextState: Unit) {
    super.componentWillUpdate(nextProps, nextState)
    prevGameStarted = nextProps[GAME_STATE].gameStarted
  }

  override fun render() = nodes {
    if (!gameState.gameStarted) {
      mainMenu()
    } else {
      layout(alignContent = ContentAlign.STRETCH, alignItems = ItemAlign.STRETCH) {
        worldController(gameState, MultiTypeMap(
            GROW to 1.0
        ))
      }
    }
    layout {
      fpsRenderer()
    }
  }

  override fun glRender(bounds: Bounds) {
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    glEnable(GL_DEPTH_TEST)

    super.glRender(bounds)
  }
}