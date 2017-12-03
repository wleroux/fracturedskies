package com.fracturedskies.game

import com.fracturedskies.engine.messages.Message
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.game.messages.*
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

class Game(coroutineContext: CoroutineContext = EmptyCoroutineContext, private val handler: suspend Game.(Message) -> Unit = {}) {
  var world: World? = null
  val globalWork = mutableListOf<Work>()

  val channel = MessageChannel(coroutineContext) { message ->
    when (message) {
      is QueueWork -> {
        globalWork.add(message.work)
      }
      is WorkAssignedToWorker -> {
        globalWork.remove(message.work)
      }
      is WorldGenerated -> {
        world = World(message.world.width, message.world.height, message.world.depth) { x, y, z ->
          message.world[x, y, z]
        }
      }
      is UpdateBlock -> {
        world!![message.pos].type = message.type
      }
      is LightUpdated -> {
        message.updates.forEach { pos, level ->
          world!![pos].skyLight = level
        }
      }
    }
    handler(message)
  }
}