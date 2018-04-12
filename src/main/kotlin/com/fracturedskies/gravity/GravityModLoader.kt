package com.fracturedskies.gravity

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext


class GravityModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(GravitySystem(initialContext).channel)
  }
}