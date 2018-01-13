package com.fracturedskies.game.worldgenerator

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.messages.*
import com.fracturedskies.game.messages.*
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

class WorldGeneratorSystem(coroutineContext: CoroutineContext, dimension: Dimension) {
  val channel = MessageChannel(coroutineContext) { message ->
    when (message) {
      is NewGameRequested -> {
        launch {
          val blocks = WorldGenerator(dimension, 0).generate()
          MessageBus.send(UpdateBlock(blocks, Cause.of(this), Context()))
        }
      }
    }
  }
}