package com.fracturedskies.game

import com.fracturedskies.engine.messages.Message
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.game.messages.*
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

class Game(coroutineContext: CoroutineContext = EmptyCoroutineContext, private val handler: suspend Game.(Message) -> Unit = {}) {
  var world: World? = null
  val globalWork = mutableListOf<Work>()
  var timeOfDay = 0f

  val channel = MessageChannel(coroutineContext) { message ->
    when (message) {
      is QueueWork -> {
        globalWork.add(message.work)
      }
      is WorkAssignedToWorker -> {
        globalWork.remove(message.work)
      }
      is WorldGenerated -> {
        world = World(message.world.dimension) { message.world[it] }
      }
      is UpdateBlock -> {
        message.updates.forEach { pos, type ->
          world!![pos].type = type
        }
      }
      is SkyLightUpdated -> {
        message.updates.forEach { pos, skyLightLevel ->
          world!![pos].skyLight = skyLightLevel
        }
      }
      is BlockLightUpdated -> {
        message.updates.forEach { pos, blockLightLevel ->
          world!![pos].blockLight = blockLightLevel
        }
      }
      is UpdateBlockWater -> {
        message.updates.forEach { (pos, waterLevel) ->
          world!![pos].waterLevel = waterLevel
        }
      }
      is TimeUpdated -> {
        timeOfDay = message.time
      }
    }
    handler(message)
  }
}