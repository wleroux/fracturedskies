package com.fracturedskies.engine.jeact

import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.jeact.event.Event
import com.fracturedskies.engine.jeact.event.Phase
import kotlin.reflect.KClass

data class Node(var vnode: VNode, val component: Component, var parent: Node?, var children: List<Node>) {
  companion object {
    val BOUNDS = Key<Bounds>("bounds")
  }
  val bounds get() = vnode.attributes[BOUNDS]
  val typeClass: KClass<Component>
    get() = vnode.typeClass

  fun unmount() {
    this.children.forEach({it.unmount()})
    this.component.willUnmount()
  }

  fun nodeFromPoint(point: Point): Node? {
    val childNode = children.mapNotNull({ it.nodeFromPoint(point) } ).firstOrNull()
    return childNode ?: if (point within this.bounds) this else null
  }

  fun render() {
    this.component.render()
    this.children.forEach({it.render()})
  }

  fun dispatch(event: Event) {
    // Capture phase
    event.phase = Phase.CAPTURE
    val ancestry = mutableListOf<Node>()
    var child: Node? = this.parent
    while (child != null) {
      ancestry.add(child)
      child = child.parent
    }

    for (node in ancestry.reversed()) {
      node.component.handler(event)
      if (event.stopPropogation)
        return
    }

    // Target phase
    event.phase = Phase.TARGET
    this.component.handler(event)
    if (event.stopPropogation)
      return

    // Bubble phase
    event.phase = Phase.BUBBLE
    for (node in ancestry) {
      node.component.handler(event)
      if (event.stopPropogation)
        return
    }
  }

  override fun toString() =
          "${vnode.typeClass.simpleName}(${vnode.attributes})"
}