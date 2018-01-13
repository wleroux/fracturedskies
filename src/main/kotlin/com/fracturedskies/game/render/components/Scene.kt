package com.fracturedskies.game.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.nodes
import com.fracturedskies.game.render.components.FpsRenderer.Companion.fpsRenderer
import com.fracturedskies.game.render.components.layout.Layout.Companion.GROW
import com.fracturedskies.game.render.components.layout.Layout.Companion.layout
import com.fracturedskies.game.render.components.world.WorldRenderer.Companion.worldRenderer

class Scene(attributes: Context) : AbstractComponent<Scene.SceneState>(attributes, SceneState()) {
  companion object {
    fun scene(): Node<SceneState> {
      return Node(::Scene)
    }
  }
  var displayBlock: Boolean
    get() = nextState?.displayBlock ?: state.displayBlock
    set(value) {nextState = (nextState?:state).copy(displayBlock = value)}
  var text: String
    get() = nextState?.text ?: state.text
    set(value) {nextState = (nextState?:state).copy(text = value)}

  override fun toNode() = nodes {
    layout(alignContent = com.fracturedskies.game.render.components.layout.ContentAlign.STRETCH, alignItems = com.fracturedskies.game.render.components.layout.ItemAlign.STRETCH) {
      worldRenderer(Context(
              GROW to 1.0
      ))
    }
    layout {
      fpsRenderer()
    }
  }

  data class SceneState(val displayBlock: Boolean = true, val text: String = "")
}