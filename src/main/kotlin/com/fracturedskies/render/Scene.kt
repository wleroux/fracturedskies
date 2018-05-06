package com.fracturedskies.render

import com.fracturedskies.api.World
import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.render.FpsRenderer.Companion.fpsRenderer
import com.fracturedskies.render.common.components.layout.*
import com.fracturedskies.render.common.components.layout.Layout.Companion.GROW
import com.fracturedskies.render.common.components.layout.Layout.Companion.layout
import com.fracturedskies.render.mainmenu.MainMenuRenderer.Companion.mainMenu
import com.fracturedskies.render.world.controller.WorldController.Companion.worldController
import org.lwjgl.opengl.GL11.*
import javax.inject.Inject

class Scene : Component<Unit>(Unit) {
  private var prevGameStarted = false

  @Inject
  private lateinit var world: World

  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean {
    if (super.shouldComponentUpdate(nextProps, nextState)) {
      return true
    } else {
      if (prevGameStarted != world.started) return true
      return false
    }
  }

  override fun componentWillUpdate(nextProps: MultiTypeMap, nextState: Unit) {
    super.componentWillUpdate(nextProps, nextState)
    prevGameStarted = world.started
  }

  override fun render() = nodes {
    if (!world.started) {
      mainMenu()
    } else {
      layout(alignContent = ContentAlign.STRETCH, alignItems = ItemAlign.STRETCH) {
        worldController(MultiTypeMap(
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