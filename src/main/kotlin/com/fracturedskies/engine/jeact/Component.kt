package com.fracturedskies.engine.jeact

import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.Node.Companion.NODES
import com.fracturedskies.engine.jeact.Node.Companion.NODE_KEY
import com.fracturedskies.engine.jeact.event.*

abstract class Component<T>(var props: MultiTypeMap, initialState: T) {
  companion object {
    fun unmount(component: Component<*>) {
      component.children.forEach { unmount(it) }
      component.componentWillUnmount()
    }

    fun <S, C: Component<S>> mount(type: (MultiTypeMap) -> C, parent: Component<*>?, props: MultiTypeMap): C {
      val component = type(props)

      component.componentWillMount()
      component.parent = parent
      component.props = props
      component.state = component.nextState ?: component.state
      component.componentDidMount()

      return component
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> toComponent(node: Node<T>, prev: Component<*>? = null, parent: Component<*>? = null): Component<T> {
      val reuseComponent = if (prev != null) prev::class == node.typeClass else false
      val component: Component<T> = if (reuseComponent) {
        prev!! as Component<T>
      } else {
        mount(node.type, parent, node.props)
      }
      update(component, node.props, !reuseComponent)
      return component
    }

    fun <T> dispatch(target: Component<T>, event: Event) {
      val ancestry = mutableListOf<Component<*>>()
      var child: Component<*>? = target.parent
      while (child != null) {
        ancestry.add(child)
        child = child.parent
      }

      // Capture phase
      event.phase = Phase.CAPTURE
      for (component in ancestry.reversed()) {
        component.handler(event)
        if (event.stopPropogation)
          return
      }

      // Target phase
      event.phase = Phase.TARGET
      target.handler(event)
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


    // Lifecycle functions
    fun <T> update(component: Component<T>, props: MultiTypeMap, forceUpdate: Boolean = false) {
      // Update component
      val prevProps = component.props
      val prevState = component.state

      val nextProps = if (props == prevProps) prevProps else props
      if (nextProps !== prevProps)
        component.componentWillReceiveProps(nextProps)
      val nextState = component.nextState ?: component.state
      if (forceUpdate || component.shouldComponentUpdate(nextProps, nextState)) {
        component.componentWillUpdate(nextProps, nextState)

        var matchedChildren = mutableListOf<Component<*>>()
        val prevChildren = component.children
        component.props = nextProps
        component.state = nextState
        component.nextState = null
        var keyCounter = 0
        component.childKeys = component.render().map({ node ->
          val key = node.props[NODE_KEY] ?: keyCounter ++
          val prevChildComponent = component.childKeys[key]
          key to toComponent(node, prevChildComponent, component)
        }).toMap()
        component.children = component.childKeys.values.toList()

        // Unmount discarded toNodes
        prevChildren.minus(component.children).forEach({ unmount(it) })
        component.componentDidUpdate(prevProps, prevState)
      } else {
        component.props = nextProps
        component.state = nextState
        component.children.forEach({ childComponent ->
          update(childComponent, childComponent.props)
        })
      }
    }
  }

  var state: T = initialState
  var nextState: T? = null
  val currentState get() = nextState ?: state

  // Component Tree
  var parent: Component<*>? = null
  var children: List<Component<*>> = mutableListOf()
  var childKeys: Map<Any, Component<*>> = mutableMapOf()

  // Mounting
  open fun componentWillMount() = Unit
  open fun componentDidMount() = Unit

  // Updating
  open fun componentWillReceiveProps(nextProps: MultiTypeMap) = Unit
  open fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: T): Boolean =
          props !== nextProps || state !== nextState
  open fun componentWillUpdate(nextProps: MultiTypeMap, nextState: T) = Unit
  open fun componentDidUpdate(prevProps: MultiTypeMap, prevState: T) = Unit

  // Unmounting
  open fun componentWillUnmount() = Unit

  // Rendering
  open fun render(): List<Node<*>> = props[NODES]

  // Event Handling
  open val handler: EventHandler = {}

  // Rendering Components
  var bounds: Bounds = Bounds(0, 0, 0, 0)
  open fun glPreferredWidth(parentWidth: Int, parentHeight: Int): Int =
      children.map({ it.glPreferredWidth(parentWidth, parentHeight) }).max() ?: 0
  open fun glPreferredHeight(parentWidth: Int, parentHeight: Int): Int =
      children.map({ it.glPreferredHeight(parentWidth, parentHeight) }).max() ?: 0
  open fun glRender(bounds: Bounds) {
    this.bounds = bounds
    for (child in children) {
      child.glRender(this.bounds)
    }
  }
  open fun glComponentFromPoint(point: Point): Component<*>? {
    return if (point within this.bounds) {
      val child = children.reversed().mapNotNull({ it.glComponentFromPoint(point) }).firstOrNull()
      child ?: this
    } else {
      null
    }
  }

  override fun toString(): String = this.javaClass.simpleName
}