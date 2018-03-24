package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.render.GameState
import com.fracturedskies.render.components.FpsRenderer.Companion.fpsRenderer
import com.fracturedskies.render.components.layout.*
import com.fracturedskies.render.components.layout.Layout.Companion.GROW
import com.fracturedskies.render.components.layout.Layout.Companion.layout
import com.fracturedskies.render.components.world.WorldController.Companion.worldController
import org.lwjgl.opengl.GL11.*

class Scene(attributes: MultiTypeMap) : Component<Unit>(attributes, Unit) {
  companion object {
    val GAME_STATE = TypedKey<GameState>("gameState")
  }
  private val gameState get() = requireNotNull(props[GAME_STATE])

  override fun toNodes() = nodes {
    layout(alignContent = ContentAlign.STRETCH, alignItems = ItemAlign.STRETCH) {
      worldController(gameState, MultiTypeMap(
          GROW to 1.0
      ))
    }
    layout {
      fpsRenderer()
    }
  }

  override fun render(bounds: Bounds) {
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    glEnable(GL_DEPTH_TEST)

    super.render(bounds)
  }
}