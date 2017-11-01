package com.fracturedskies.engine.jeact

import kotlin.reflect.KClass

data class Node(val vnode: VNode, val component: Component, val children: List<Node>) {
  val typeClass: KClass<Component>
    get() = vnode.typeClass
  override fun toString(): String {
    return if (children.isEmpty()) {
      "${vnode.typeClass.simpleName}(${vnode.attributes}) [$component]"
    } else {
      "${vnode.typeClass.simpleName}(${vnode.attributes}) [$component]\n" +
              children.map({ it -> it.toString() }).joinToString(separator = "\n").split("\n").map({ "  $it" }).joinToString(separator = "\n")
    }
  }
}

fun Node.unmount() {
  this.children.forEach({it.unmount()})
  this.component.willUnmount()
}
