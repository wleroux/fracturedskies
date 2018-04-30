package com.fracturedskies.render

import com.fracturedskies.api.World
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.render.FpsRenderer.Companion.fpsRenderer
import com.fracturedskies.render.common.components.layout.*
import com.fracturedskies.render.common.components.layout.Layout.Companion.GROW
import com.fracturedskies.render.common.components.layout.Layout.Companion.layout
import com.fracturedskies.render.mainmenu.MainMenuRenderer.Companion.mainMenu
import com.fracturedskies.render.world.controller.WorldController.Companion.worldController
import org.lwjgl.opengl.GL11.*

class Scene(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    val WORLD = TypedKey<World>("world")
    val DIRTY_FLAGS = TypedKey<DirtyFlags>("dirtyFlags")
  }

  private var prevGameStarted = false
  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean {
    if (super.shouldComponentUpdate(nextProps, nextState)) {
      return true
    } else {
      if (prevGameStarted != props[WORLD].started) return true
      return false
    }
  }

  override fun componentWillUpdate(nextProps: MultiTypeMap, nextState: Unit) {
    super.componentWillUpdate(nextProps, nextState)
    prevGameStarted = nextProps[WORLD].started
  }

  override fun render() = nodes {
    if (!props[WORLD].started) {
      mainMenu(props[WORLD])
    } else {
      layout(alignContent = ContentAlign.STRETCH, alignItems = ItemAlign.STRETCH) {
        worldController(props[WORLD], props[DIRTY_FLAGS], MultiTypeMap(
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