package com.fracturedskies.worldgenerator

import com.fracturedskies.api.*
import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.messages.*
import kotlin.coroutines.experimental.CoroutineContext

class WorldGeneratorSystem(coroutineContext: CoroutineContext) {
  val channel = MessageChannel(coroutineContext) { message ->
    when (message) {
      is NewGameRequested -> {
        val blocks = WorldGenerator(message.dimension, 0).generate()
        MessageBus.send(UpdateBlock(blocks, Cause.of(this), MultiTypeMap()))
      }
    }
  }
}