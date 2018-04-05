package com.fracturedskies.colonist

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext

class ColonistModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(ColonistTaskSystem(initialContext).channel)
    register(ColonistSystem(initialContext).channel)
  }
}