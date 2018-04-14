package com.fracturedskies.worldgenerator

import com.fracturedskies.api.NewGameRequested
import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageChannel
import kotlin.coroutines.experimental.CoroutineContext

class WorldGeneratorModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(MessageChannel(initialContext) { message ->
      if (message is NewGameRequested) {
        WorldGenerator(message.dimension, message.seed).generate()
      }
    })
  }
}