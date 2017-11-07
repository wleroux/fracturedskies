package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.nodes
import com.fracturedskies.render.components.FpsRenderer.Companion.fpsRenderer
import com.fracturedskies.render.components.layout.ContentAlign
import com.fracturedskies.render.components.layout.ItemAlign
import com.fracturedskies.render.components.layout.Layout.Companion.GROW
import com.fracturedskies.render.components.layout.Layout.Companion.layout
import com.fracturedskies.render.components.world.WorldRenderer.Companion.worldRenderer

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
    layout(alignContent = ContentAlign.STRETCH, alignItems = ItemAlign.STRETCH) {
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