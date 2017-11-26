package com.fracturedskies.game

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus
import com.fracturedskies.game.messages.NewGameRequested
import com.fracturedskies.game.messages.WorldGenerated
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

class WorldGeneratorSystem(coroutineContext: CoroutineContext = CommonPool) {
  val game = Game(coroutineContext) { message ->
    when (message) {
      is NewGameRequested -> {
        launch {
          val world = WorldGenerator(128, 256, 128, 0).generate()
          MessageBus.send(WorldGenerated(world, Cause.of(this), Context()))
        }
      }
    }
  }
  val channel get() = game.channel
}