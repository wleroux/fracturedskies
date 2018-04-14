package com.fracturedskies.render.common.components

import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.Node.Companion.NODE_KEY


class Key(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.key(key: Any, additionalContext: MultiTypeMap = MultiTypeMap(), block: Node.Builder<*>.() -> Unit = {}) {
      nodes.add(Node(::Key, MultiTypeMap(
          NODE_KEY to key
      ).with(additionalContext), block))
    }
  }
}