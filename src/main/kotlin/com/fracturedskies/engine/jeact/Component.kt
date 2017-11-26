package com.fracturedskies.engine.jeact

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.Node.Companion.NODES
import com.fracturedskies.engine.jeact.event.Event
import com.fracturedskies.engine.jeact.event.EventHandler
import com.fracturedskies.engine.jeact.event.Phase

interface Component<T> {
  var attributes: Context
  var state: T
  var nextState: T?
  val handler: EventHandler

  // Bounds
  var bounds: Bounds
  fun preferredWidth(parentWidth: Int, parentHeight: Int): Int =
          children.map({ it.preferredWidth(parentWidth, parentHeight) }).max() ?: 0
  fun preferredHeight(parentWidth: Int, parentHeight: Int): Int =
          children.map({ it.preferredHeight(parentWidth, parentHeight) }).max() ?: 0

  // Component Tree
  var parent: Component<*>?
  var children: List<Component<*>>

  // Mounting
  fun willMount() = Unit
  fun didMount() = Unit

  // Updating
  fun willReceiveProps(nextAttributes: Context) = Unit
  fun shouldUpdate(nextAttributes: Context, nextState: T): Boolean =
          attributes !== nextAttributes || state !== nextState
  fun willUpdate(nextAttributes: Context, nextState: T) = Unit
  fun didUpdate(prevAttributes: Context, prevState: T) = Unit

  // Unmounting
  fun willUnmount() = Unit

  fun toNode(): List<Node<*>> = requireNotNull(attributes[NODES])
  fun render(bounds: Bounds)

  // Lifecycle functions
  fun update(attributes: Context, forceUpdate: Boolean = false) {
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
      this.children = this.toNode().mapIndexed({ index, node ->
        val prevChildComponent = this.children.getOrNull(index)
        node.toComponent(prevChildComponent, this)
      })

      // Unmount discarded toNode
      prevChildren.minus(this.children).forEach({ it.unmount() })
      this.didUpdate(prevAttributes, prevState)
    } else {
      this.children.forEach({ childComponent ->
        childComponent.update(childComponent.attributes)
      })
    }
  }

  fun unmount() {
    this.children.forEach({it.unmount()})
    this.willUnmount()
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

  fun componentFromPoint(point: Point): Component<*>? {
    return if (point within this.bounds) {
      val child = children.reversed().mapNotNull({ it.componentFromPoint(point) }).firstOrNull()
      child ?: this
    } else {
      null
    }
  }
}

abstract class AbstractComponent<T>(override var attributes: Context, initialState: T) : Component<T> {
  override var state: T = initialState
  override var nextState: T? = null
  override val handler: EventHandler = {}
  override var parent: Component<*>? = null
  override var children: List<Component<*>> = listOf()
  override lateinit var bounds: Bounds

  override fun render(bounds: Bounds) {
    this.bounds = bounds
    for (child in children) {
      child.render(this.bounds)
    }
  }

  override fun toString(): String = this.javaClass.simpleName
}

fun <T> mount(type: (Context) -> Component<T>, parent: Component<*>?, attributes: Context): Component<T> {
  val component = type(attributes)

  component.willMount()
  component.parent = parent
  component.attributes = attributes
  component.state = component.nextState ?: component.state
  component.didMount()
  return component
}
