package com.fracturedskies.water

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext


class WaterModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(WaterSystem(initialContext).channel)
  }
}