package com.fracturedskies.engine.jeact

import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.Node.Companion.NODES
import com.fracturedskies.engine.jeact.event.*

abstract class Component<T>(var props: MultiTypeMap, initialState: T) {
  companion object {
    fun unmount(component: Component<*>) {
      component.children.forEach { unmount(it) }
      component.willUnmount()
    }

    fun <S, C: Component<S>> mount(type: (MultiTypeMap) -> C, parent: Component<*>?, props: MultiTypeMap): C {
      val component = type(props)

      component.willMount()
      component.parent = parent
      component.props = props
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
        mount(node.type, parent, node.props)
      }
      component.update(node.props, !reuseComponent)
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
  open fun willReceiveProps(nextProps: MultiTypeMap) = Unit
  open fun shouldUpdate(nextProps: MultiTypeMap, nextState: T): Boolean =
          props !== nextProps || state !== nextState
  open fun willUpdate(nextProps: MultiTypeMap, nextState: T) = Unit
  open fun didUpdate(prevProps: MultiTypeMap, prevState: T) = Unit

  // Unmounting
  open fun willUnmount() = Unit

  // Rendering Components
  var bounds: Bounds = Bounds(0, 0, 0, 0)
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
  open fun toNodes(): List<Node<*>> = requireNotNull(props[NODES])
  fun update(props: MultiTypeMap, forceUpdate: Boolean = false) {
    // Update component
    val prevProps = this.props
    val prevState = this.state

    val nextProps = if (props == prevProps) prevProps else props
    if (nextProps !== prevProps)
      this.willReceiveProps(nextProps)
    val nextState = this.nextState ?: this.state
    if (forceUpdate || this.shouldUpdate(nextProps, nextState)) {
      this.willUpdate(nextProps, nextState)

      val prevChildren = this.children
      this.props = nextProps
      this.state = nextState
      this.nextState = null
      this.children = this.toNodes().mapIndexed({ index, node ->
        val prevChildComponent = this.children.getOrNull(index)
        toComponent(node, prevChildComponent, this)
      })

      // Unmount discarded toNodes
      prevChildren.minus(this.children).forEach({ unmount(it) })
      this.didUpdate(prevProps, prevState)
    } else {
      this.children.forEach({ childComponent ->
        childComponent.update(childComponent.props)
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