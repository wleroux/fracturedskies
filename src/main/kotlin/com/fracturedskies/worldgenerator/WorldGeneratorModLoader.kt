package com.fracturedskies.worldgenerator

import com.fracturedskies.api.*
import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.engine.messages.MessageChannel
import kotlin.coroutines.experimental.CoroutineContext

class WorldGeneratorModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(MessageChannel(initialContext) { message ->
      if (message is NewGameRequested) {
        val blocks = WorldGenerator(message.dimension, 0).generate()
        send(UpdateBlock(blocks, message.cause, message.context))
      }
    })
  }
}