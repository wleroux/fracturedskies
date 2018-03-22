package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.render.components.FpsRenderer.Companion.fpsRenderer
import com.fracturedskies.render.components.Scene.SceneState
import com.fracturedskies.render.components.layout.*
import com.fracturedskies.render.components.layout.Layout.Companion.GROW
import com.fracturedskies.render.components.layout.Layout.Companion.layout
import com.fracturedskies.render.components.world.WorldRenderer.Companion.worldRenderer

class Scene(attributes: MultiTypeMap) : Component<SceneState>(attributes, SceneState()) {
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

  override fun toNodes() = nodes {
    layout(alignContent = ContentAlign.STRETCH, alignItems = ItemAlign.STRETCH) {
      worldRenderer(MultiTypeMap(
              GROW to 1.0
      ))
    }
    layout {
//      input(initialValue = "Testing", onTextChanged = {message-> println(message)})
      fpsRenderer()
    }
  }

  data class SceneState(val displayBlock: Boolean = true, val text: String = "")
}