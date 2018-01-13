package com.fracturedskies.game.worldgenerator

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.messages.*
import com.fracturedskies.game.messages.*
import kotlinx.coroutines.experimental.*
import kotlin.coroutines.experimental.CoroutineContext

class WorldGeneratorSystem(coroutineContext: CoroutineContext = CommonPool) {
  val channel = MessageChannel(coroutineContext) { message ->
    when (message) {
      is NewGameRequested -> {
        launch {
          val world = WorldGenerator(Dimension(128, 256, 128), 0).generate()
          MessageBus.send(WorldGenerated(world, Cause.of(this), Context()))
        }
      }
    }
  }
}