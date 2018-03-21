package com.fracturedskies.engine.jeact

import com.fracturedskies.engine.collections.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.*

data class Node<T> private constructor(val type: (MultiTypeMap) -> Component<T>, val attributes: MultiTypeMap) {
  companion object {
    val NODES = TypedKey<List<Node<*>>>("nodes")
    operator fun <T> invoke(type: (MultiTypeMap) -> Component<T>, context: MultiTypeMap = MultiTypeMap(), block: Builder<T>.() -> Unit = {}) = Builder(type, context).apply(block).build()
  }

  class Builder<T> internal constructor(private val type: (MultiTypeMap) -> Component<T>, private val context: MultiTypeMap) {
    val nodes = mutableListOf<Node<*>>()
    fun build(): Node<T> {
      return Node(type, context.with(NODES to nodes))
    }
  }

  @Suppress("UNCHECKED_CAST")
  private val typeClass: KClass<Component<T>> = type.reflect()!!.returnType.jvmErasure as KClass<Component<T>>
  override fun toString() = "${typeClass.simpleName}($attributes)"
  override fun hashCode() = 31 * type.hashCode() + attributes.hashCode()
  override fun equals(other: Any?): Boolean {
    if (this === other)
      return true
    return when (other) {
      is Node<*> -> this.type == other.type && this.attributes == other.attributes
      else -> false
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun toComponent(prev: Component<*>? = null, parent: Component<*>? = null): Component<T> {
    val reuseComponent = if (prev != null) prev::class == this.typeClass else false
    val component: Component<T> = if (reuseComponent) {
      prev!! as Component<T>
    } else {
      prev?.unmount()
      mount(type, parent, this.attributes)
    }
    component.update(this.attributes, !reuseComponent)
    return component
  }
}

class NodeCollector(attributes: MultiTypeMap) : AbstractComponent<Unit>(attributes, Unit)
fun nodes(block: Node.Builder<*>.() -> Unit): List<Node<*>> {
  return requireNotNull(Node(::NodeCollector, MultiTypeMap(), block).attributes[Node.NODES])
}