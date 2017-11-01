package com.fracturedskies.engine.jeact

import com.fracturedskies.engine.collections.TypedKey
import com.fracturedskies.engine.collections.TypedMap
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

data class VNode private constructor(val type: (TypedMap) -> Component, val attributes: TypedMap) {
  companion object {
    val CHILDREN = TypedKey<List<VNode>>("children")
    operator fun invoke(type: (TypedMap) -> Component, vararg attributes: Pair<TypedKey<*>, Any>, block: Builder.() -> Unit = {}) = Builder(type, *attributes).apply(block).build()
  }

  val children: List<VNode>
    get() = requireNotNull(attributes[CHILDREN])
  @Suppress("UNCHECKED_CAST")
  val typeClass: KClass<Component> = type.reflect()!!.returnType.jvmErasure as KClass<Component>

  override fun toString(): String {
    val curr = "${typeClass.simpleName}($attributes)"
    return if (children.isEmpty()) {
      curr
    } else {
      curr + "\n" + children.map({ it -> it.toString() }).joinToString(separator = "\n").split("\n").map({ "  $it" }).joinToString(separator = "\n")
    }
  }

  class Builder internal constructor(private val type: (TypedMap) -> Component, vararg attributes: Pair<TypedKey<*>, Any>) {
    private val attributes = attributes
    val children = mutableListOf<VNode>()
    fun build(): VNode {
      return VNode(type, TypedMap(mapOf(*attributes, CHILDREN to children)))
    }
  }
}

// Create new node from vnode
fun VNode.toNode(prevNode: Node? = null): Node {
  val prevComponent = prevNode?.component
  val isNewComponent = !(this.typeClass == prevNode?.typeClass && prevComponent != null)
  val component = if (!isNewComponent) {
    // Reuse previous component
    prevComponent!!
  } else {
    // Unmount previous component
    prevNode?.unmount()

    // Mount new component
    val newComponent = this.type(this.attributes)
    newComponent.willMount()
    newComponent.attributes = this.attributes
    newComponent.state = newComponent.nextState ?: newComponent.state
    newComponent.didMount()
    newComponent
  }

  // Update component
  val prevAttributes = component.attributes
  val prevState = component.state

  val nextAttributes = this.attributes
  if (nextAttributes !== prevAttributes)
    component.willReceiveProps(nextAttributes)
  val nextState = component.nextState ?: component.state
  if (isNewComponent || component.shouldUpdate(nextAttributes, nextState)) {
    component.willUpdate(nextAttributes, nextState)
    component.attributes = nextAttributes
    component.state = nextState

    // Get Child VNodes
    val childrenNodes = component.children().mapIndexed({
      index, vnode -> vnode.toNode(if (isNewComponent) { null } else {prevNode?.children?.getOrNull(index)})
    })

    // Unmount discarded children
    if (!isNewComponent) {
      prevNode?.children?.drop(childrenNodes.size)?.forEach({ it.unmount() })
    }

    component.didUpdate(prevAttributes, prevState)

    return Node(this, component, childrenNodes)
  } else {
    return Node(this, component, prevNode!!.children.map({ node -> node.vnode.toNode(node) }))
  }
}