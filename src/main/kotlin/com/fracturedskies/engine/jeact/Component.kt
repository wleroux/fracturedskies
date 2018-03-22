package com.fracturedskies.engine.jeact

import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.Node.Companion.NODES
import com.fracturedskies.engine.jeact.event.*

abstract class Component<T>(var attributes: MultiTypeMap, initialState: T) {
  companion object {
    fun unmount(component: Component<*>) {
      component.children.forEach { unmount(it) }
      component.willUnmount()
    }

    fun <S, C: Component<S>> mount(type: (MultiTypeMap) -> C, parent: Component<*>?, attributes: MultiTypeMap): C {
      val component = type(attributes)

      component.willMount()
      component.parent = parent
      component.attributes = attributes
      component.state = component.nextState ?: component.state
      component.didMount()

      return component
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> toComponent(node: Node<T>, prev: Component<*>? = null, parent: Component<*>? = null): Component<T> {
      val reuseComponent = if (prev != null) prev::class == node.typeClass else false
      val component: Component<T> = if (reuseComponent) {
        prev!! as Component<T>
      } else {
        if (prev != null) unmount(prev)
        mount(node.type, parent, node.attributes)
      }
      component.update(node.attributes, !reuseComponent)
      return component
    }
  }

  var state: T = initialState
  var nextState: T? = null
  open val handler: EventHandler = {}

  // Component Tree
  var parent: Component<*>? = null
  var children: List<Component<*>> = listOf()

  // Mounting
  open fun willMount() = Unit
  open fun didMount() = Unit

  // Updating
  open fun willReceiveProps(nextAttributes: MultiTypeMap) = Unit
  open fun shouldUpdate(nextAttributes: MultiTypeMap, nextState: T): Boolean =
          attributes !== nextAttributes || state !== nextState
  open fun willUpdate(nextAttributes: MultiTypeMap, nextState: T) = Unit
  open fun didUpdate(prevAttributes: MultiTypeMap, prevState: T) = Unit

  // Unmounting
  open fun willUnmount() = Unit

  // Rendering Components
  lateinit var bounds: Bounds
  open fun preferredWidth(parentWidth: Int, parentHeight: Int): Int =
      children.map({ it.preferredWidth(parentWidth, parentHeight) }).max() ?: 0
  open fun preferredHeight(parentWidth: Int, parentHeight: Int): Int =
      children.map({ it.preferredHeight(parentWidth, parentHeight) }).max() ?: 0
  open fun render(bounds: Bounds) {
    this.bounds = bounds
    for (child in children) {
      child.render(this.bounds)
    }
  }
  open fun componentFromPoint(point: Point): Component<*>? {
    return if (point within this.bounds) {
      val child = children.reversed().mapNotNull({ it.componentFromPoint(point) }).firstOrNull()
      child ?: this
    } else {
      null
    }
  }

  // Lifecycle functions
  open fun toNodes(): List<Node<*>> = requireNotNull(attributes[NODES])
  fun update(attributes: MultiTypeMap, forceUpdate: Boolean = false) {
    // Update component
    val prevAttributes = this.attributes
    val prevState = this.state

    val nextAttributes = if (attributes == prevAttributes) prevAttributes else attributes
    if (nextAttributes !== prevAttributes)
      this.willReceiveProps(nextAttributes)
    val nextState = this.nextState ?: this.state
    if (forceUpdate || this.shouldUpdate(nextAttributes, nextState)) {
      this.willUpdate(nextAttributes, nextState)

      val prevChildren = this.children
      this.attributes = nextAttributes
      this.state = nextState
      this.nextState = null
      this.children = this.toNodes().mapIndexed({ index, node ->
        val prevChildComponent = this.children.getOrNull(index)
        toComponent(node, prevChildComponent, this)
      })

      // Unmount discarded toNodes
      prevChildren.minus(this.children).forEach({ unmount(it) })
      this.didUpdate(prevAttributes, prevState)
    } else {
      this.children.forEach({ childComponent ->
        childComponent.update(childComponent.attributes)
      })
    }
  }

  fun dispatch(event: Event) {
    // Capture phase
    event.phase = Phase.CAPTURE
    val ancestry = mutableListOf<Component<*>>()
    var child: Component<*>? = parent
    while (child != null) {
      ancestry.add(child)
      child = child.parent
    }

    for (component in ancestry.reversed()) {
      component.handler(event)
      if (event.stopPropogation)
        return
    }

    // Target phase
    event.phase = Phase.TARGET
    handler(event)
    if (event.stopPropogation)
      return

    // Bubble phase
    event.phase = Phase.BUBBLE
    for (component in ancestry) {
      component.handler(event)
      if (event.stopPropogation)
        return
    }
  }

  override fun toString(): String = this.javaClass.simpleName
}