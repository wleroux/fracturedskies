package com.fracturedskies.time

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext


class TimeModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(TimeSystem(initialContext).channel)
  }
}