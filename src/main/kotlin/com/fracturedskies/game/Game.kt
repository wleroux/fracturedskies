package com.fracturedskies.game

import com.fracturedskies.engine.messages.Message
import com.fracturedskies.game.messages.UpdateBlock
import com.fracturedskies.game.messages.WorldGenerated

class Game {
  var world: World? = null

  operator fun invoke(message: Message) {
    when (message) {
      is WorldGenerated -> {
        world = World(message.world.width, message.world.height, message.world.depth) { x, y, z ->
          Block(message.world[x, y, z].type)
        }
      }
      is UpdateBlock -> {
        world!![message.x, message.y, message.z].type = message.type
      }
    }
  }
}
