package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.nodes
import com.fracturedskies.render.components.AlternatingBlock.Companion.alternatingBlock
import com.fracturedskies.render.components.Button.Companion.button
import com.fracturedskies.render.components.FpsRenderer.Companion.fpsRenderer
import com.fracturedskies.render.components.TextRenderer.Companion.textRenderer
import com.fracturedskies.render.components.layout.ContentAlign
import com.fracturedskies.render.components.layout.FlexBox.Companion.flexBox
import com.fracturedskies.render.components.layout.JustifyContent

class Scene(attributes: Context) : AbstractComponent<Boolean>(attributes, true) {
  companion object {
    fun scene(): Node<Boolean> {
      return Node(::Scene)
    }
  }
  var displayBlock: Boolean
    get() = nextState ?: state
    set(value) {nextState = value}
  override fun toNode(): List<Node<*>> {
    return nodes {
      flexBox(justifyContent = JustifyContent.SPACE_AROUND, alignContent = ContentAlign.CENTER) {
        button(onClick = {_ -> displayBlock = !displayBlock}) {
          textRenderer("Toggle Block Visibility")
        }
        if (displayBlock) {
          alternatingBlock()
        }
      }
      flexBox {
        fpsRenderer()
      }
    }
  }
}