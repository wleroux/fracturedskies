package com.fracturedskies.treefall

import com.fracturedskies.api.NewGameRequested
import com.fracturedskies.engine.messages.MessageChannel
import kotlin.coroutines.experimental.CoroutineContext

class TreeFallSystem(context: CoroutineContext) {
  var initialized = false
  lateinit var world: TreeFallWorldState
  val channel = MessageChannel(context) { message ->
    when (message) {
      is NewGameRequested -> {
        world = TreeFallWorldState(message.dimension)
        initialized = true
      }
      else -> if (initialized) world.process(message)
    }
  }
}
