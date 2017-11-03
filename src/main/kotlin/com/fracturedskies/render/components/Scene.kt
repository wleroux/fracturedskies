package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds
import com.fracturedskies.engine.jeact.Component.Companion.BOUNDS
import com.fracturedskies.engine.jeact.Node

class Scene(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  override fun toNode(): List<Node<*>> {
    return listOf(Node(::AlternatingBlock, Context(BOUNDS to Bounds(bounds.x + bounds.width / 4, bounds.y + bounds.height / 4, bounds.width / 2, bounds.height / 2))) {
      nodes.addAll(super.toNode())
    })
  }
}