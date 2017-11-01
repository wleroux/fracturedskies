package com.fracturedskies.engine.jeact

import com.fracturedskies.engine.collections.TypedMap
import com.fracturedskies.engine.jeact.VNode.Companion.CHILDREN

interface Component {
  var attributes: TypedMap
  var state: Any?
  var nextState: Any?

  // Mounting
  fun willMount() = Unit
  fun didMount() = Unit

  // Updating
  fun willReceiveProps(nextAttributes: TypedMap) = Unit
  fun shouldUpdate(nextAttributes: TypedMap, nextState: Any?): Boolean =
          attributes !== nextAttributes || state !== nextState
  fun willUpdate(nextAttributes: TypedMap, nextState: Any?) = Unit
  fun didUpdate(prevAttributes: TypedMap, prevState: Any?) = Unit

  // Unmounting
  fun willUnmount() = Unit

  fun children(): List<VNode> = requireNotNull(attributes[CHILDREN])
}