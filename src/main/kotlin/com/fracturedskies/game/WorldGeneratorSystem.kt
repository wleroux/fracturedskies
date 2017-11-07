package com.fracturedskies.game

import com.fracturedskies.engine.GameSystem
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.Message
import com.fracturedskies.engine.messages.MessageBus.dispatch
import com.fracturedskies.game.messages.NewGameRequested
import com.fracturedskies.game.messages.WorldGenerated
import kotlinx.coroutines.experimental.CommonPool
import kotlin.coroutines.experimental.CoroutineContext

class WorldGeneratorSystem(coroutineContext: CoroutineContext = CommonPool) : GameSystem(coroutineContext) {
  suspend override fun invoke(message: Message) {
    when (message) {
      is NewGameRequested -> {
        val world = WorldGenerator(128, 256, 128, 0).generate()
        dispatch(WorldGenerated(world, Cause.of(this), Context()))
      }
    }
  }
}