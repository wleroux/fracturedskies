package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds
import com.fracturedskies.engine.jeact.Node.Companion.BOUNDS
import com.fracturedskies.engine.jeact.VNode

class Scene(attributes: Context) : AbstractComponent(attributes) {
  val bounds get() = requireNotNull(attributes[BOUNDS])

  override fun children(): List<VNode> {
    return listOf(VNode(::AlternatingBlock, Context(BOUNDS to Bounds(bounds.x + bounds.width / 4, bounds.y + bounds.height / 4, bounds.width / 2, bounds.height / 2))) {
      children.addAll(super.children())
    })
  }
}