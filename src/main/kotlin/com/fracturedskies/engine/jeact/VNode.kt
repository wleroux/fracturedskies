package com.fracturedskies.engine.jeact

import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.collections.Context
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

data class VNode private constructor(val type: (Context) -> Component, val attributes: Context) {
  companion object {
    val CHILDREN = Key<List<VNode>>("children")
    operator fun invoke(type: (Context) -> Component, context: Context = Context(), block: Builder.() -> Unit = {}) = Builder(type, context).apply(block).build()
  }

  class Builder internal constructor(private val type: (Context) -> Component, private val context: Context) {
    val children = mutableListOf<VNode>()
    fun build(): VNode {
      return VNode(type, context.with(CHILDREN to children))
    }
  }

  @Suppress("UNCHECKED_CAST")
  val typeClass: KClass<Component> = type.reflect()!!.returnType.jvmErasure as KClass<Component>

  override fun toString() =
          "${typeClass.simpleName}($attributes)"

  override fun equals(other: Any?): Boolean {
    if (this === other)
      return true
    return when (other) {
      is VNode -> this.type == other.type && this.attributes == other.attributes
      else -> false
    }
  }

  // Create new node from vnode
  fun toNode(prevNode: Node? = null, parentNode: Node? = null): Node {
    val reuseNode = this.typeClass == prevNode?.typeClass
    val node = if (reuseNode) {
      prevNode!!
    } else {
      // Unmount previous node
      prevNode?.unmount()

      // Mount new component
      val newComponent = this.type(this.attributes)
      newComponent.willMount()
      newComponent.attributes = this.attributes
      newComponent.state = newComponent.nextState ?: newComponent.state
      newComponent.didMount()
      Node(this, newComponent, parentNode, listOf())
    }
    node.vnode = this

    // Update component
    val prevAttributes = node.component.attributes
    val prevState = node.component.state

    val nextAttributes = if (this.attributes == prevAttributes) prevAttributes else this.attributes
    if (nextAttributes !== prevAttributes)
      node.component.willReceiveProps(nextAttributes)
    val nextState = node.component.nextState ?: node.component.state
    if (!reuseNode || node.component.shouldUpdate(nextAttributes, nextState)) {
      node.component.willUpdate(nextAttributes, nextState)

      val prevChildren = node.children
      node.component.attributes = nextAttributes
      node.component.state = nextState
      node.component.nextState = null
      node.children = node.component.children().mapIndexed({ index, vnode ->
        val prevChildNode = node.children.getOrNull(index)
        vnode.toNode(prevChildNode, node)
      })

      // Unmount discarded children
      prevChildren.minus(node.children).forEach({ it.unmount() })
      node.component.didUpdate(prevAttributes, prevState)
      return node
    } else {
      node.children = node.children.map({ childNode ->
        childNode.vnode.toNode(childNode, node)
      })
      return node
    }
  }
}