package com.fracturedskies.engine.jeact

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.VNode.Companion.CHILDREN
import com.fracturedskies.engine.jeact.event.EventHandler

interface Component {
  var attributes: Context
  var state: Any?
  var nextState: Any?
  val handler: EventHandler

  // Mounting
  fun willMount() = Unit
  fun didMount() = Unit

  // Updating
  fun willReceiveProps(nextAttributes: Context) = Unit
  fun shouldUpdate(nextAttributes: Context, nextState: Any?): Boolean =
          attributes !== nextAttributes || state !== nextState
  fun willUpdate(nextAttributes: Context, nextState: Any?) = Unit
  fun didUpdate(prevAttributes: Context, prevState: Any?) = Unit

  // Unmounting
  fun willUnmount() = Unit

  fun children(): List<VNode> = requireNotNull(attributes[CHILDREN])
  fun render() = Unit
}

abstract class AbstractComponent(override var attributes: Context) : Component {
  override var state: Any? = null
  override var nextState: Any? = null
  override val handler: EventHandler = {}

  override fun toString(): String = this.javaClass.simpleName
}