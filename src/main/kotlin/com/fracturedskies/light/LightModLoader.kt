package com.fracturedskies.light

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext


class LightModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(blockLightSystem(initialContext))
    register(skyLightSystem(initialContext))
  }
}