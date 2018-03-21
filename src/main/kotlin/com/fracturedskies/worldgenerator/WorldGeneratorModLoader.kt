package com.fracturedskies.worldgenerator

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext


class WorldGeneratorModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(WorldGeneratorSystem(initialContext).channel)
  }
}