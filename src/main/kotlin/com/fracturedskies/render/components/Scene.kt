package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.nodes
import com.fracturedskies.render.components.AlternatingBlock.Companion.alternatingBlock
import com.fracturedskies.render.components.Flex.Companion.flex
import com.fracturedskies.render.components.FpsRenderer.Companion.fpsRenderer

class Scene(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    fun scene(): Node<Unit> {
      return Node(::Scene)
    }
  }

  override fun toNode(): List<Node<*>> {
    return nodes {
      flex(justifyContent = JustifyContent.SPACE_AROUND, alignContent = ContentAlign.CENTER) {
        alternatingBlock()
        alternatingBlock()
        alternatingBlock()
      }
      fpsRenderer()
    }
  }
}