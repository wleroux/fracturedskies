package com.fracturedskies.gravity

import com.fracturedskies.World
import com.fracturedskies.api.*
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.send
import kotlin.coroutines.experimental.CoroutineContext


class GravitySystem(context: CoroutineContext) {
  var initialized = false
  lateinit var state: World
  val channel = MessageChannel(context) { message ->
    when (message) {
      is NewGameRequested -> {
        state = World(message.dimension)
        initialized = true
      }
      is Update -> {
        if (initialized) {
          state.items
              .filterValues { it.position != null }
              .forEach { id, item ->
            val belowPos = item.position!! - Vector3i.AXIS_Y
            if (state.blocked.has(belowPos) && !state.blocked[belowPos]) {
              send(ItemMoved(id, belowPos, Cause.of(this)))
            }
          }
        }
      }
      else -> if (initialized) state.process(message)
    }
  }
}
