package com.fracturedskies.engine.jeact

import com.fracturedskies.engine.collections.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.*

class Node<T> private constructor(val type: (MultiTypeMap) -> Component<T>, val props: MultiTypeMap) {
  companion object {
    val NODE_KEY = TypedKey<Any?>("nodeKey")
    val NODES = TypedKey<List<Node<*>>>("nodes")
    operator fun <T> invoke(type: (MultiTypeMap) -> Component<T>, props: MultiTypeMap = MultiTypeMap(), block: Builder<T>.() -> Unit = {}) = Builder(type, props).apply(block).build()
  }

  class Builder<T> internal constructor(private val type: (MultiTypeMap) -> Component<T>, private val props: MultiTypeMap) {
    val nodes = mutableListOf<Node<*>>()
    fun build(): Node<T> {
      return Node(type, props.with(NODES to nodes))
    }
  }

  @Suppress("UNCHECKED_CAST")
  val typeClass: KClass<Component<T>> = type.reflect()!!.returnType.jvmErasure as KClass<Component<T>>
  override fun toString() = "${typeClass.simpleName}($props)"
  override fun hashCode() = 31 * type.hashCode() + props.hashCode()
  override fun equals(other: Any?): Boolean {
    if (this === other)
      return true
    return when (other) {
      is Node<*> -> this.type == other.type && this.props == other.props
      else -> false
    }
  }
}

class NodeCollector(attributes: MultiTypeMap) : Component<Unit>(attributes, Unit)
fun nodes(block: Node.Builder<*>.() -> Unit): List<Node<*>> {
  return requireNotNull(Node(::NodeCollector, MultiTypeMap(), block).props[Node.NODES])
}