package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.render.components.Flex.Companion.ALIGN_CONTENT
import com.fracturedskies.render.components.Flex.Companion.ALIGN_ITEMS
import com.fracturedskies.render.components.Flex.Companion.DIRECTION
import com.fracturedskies.render.components.Flex.Companion.JUSTIFY_CONTENT
import com.fracturedskies.render.components.Flex.Companion.WRAP

class Scene(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  override fun toNode(): List<Node<*>> {
    return listOf(Node(::Flex, Context(
      DIRECTION to Flex.Direction.ROW,
      JUSTIFY_CONTENT to Flex.JustifyContent.SPACE_AROUND,
      ALIGN_ITEMS to Flex.ItemAlign.START,
      ALIGN_CONTENT to Flex.ContentAlign.CENTER,
      WRAP to Flex.Wrap.WRAP
    )) {
      nodes.addAll(listOf(
              Node(::AlternatingBlock),
              Node(::AlternatingBlock),
              Node(::AlternatingBlock),
              Node(::AlternatingBlock),
              Node(::AlternatingBlock)
      ))
    }, Node(::FpsRenderer))
  }
}