package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.nodes
import com.fracturedskies.render.components.AlternatingBlock.Companion.alternatingBlock
import com.fracturedskies.render.components.FpsRenderer.Companion.fpsRenderer
import com.fracturedskies.render.components.TextRenderer.Companion.textRenderer
import com.fracturedskies.render.components.button.Button.Companion.button
import com.fracturedskies.render.components.input.Input.Companion.input
import com.fracturedskies.render.components.layout.ContentAlign
import com.fracturedskies.render.components.layout.Direction
import com.fracturedskies.render.components.layout.ItemAlign
import com.fracturedskies.render.components.layout.JustifyContent
import com.fracturedskies.render.components.layout.Layout.Companion.layout

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

  override fun toNode(): List<Node<*>> {
    return nodes {
      layout(justifyContent = JustifyContent.SPACE_AROUND, alignContent = ContentAlign.CENTER) {
        layout(direction = Direction.COLUMN_REVERSE, alignItems = ItemAlign.CENTER) {
          input("Input Box!", {text ->
            this@Scene.text = text
          })
          textRenderer("Input Text: $text")
        }
        layout(direction = Direction.COLUMN_REVERSE, alignItems = ItemAlign.CENTER) {
          button(onClick = {_ -> displayBlock = !displayBlock}) {
            textRenderer("Toggle Block Visibility")
          }
          if (displayBlock) {
            alternatingBlock()
          }
        }
      }
      layout {
        fpsRenderer()
      }
    }
  }

  data class SceneState(val displayBlock: Boolean = true, val text: String = "")
}