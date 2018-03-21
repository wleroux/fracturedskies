package com.fracturedskies.worker

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext


class WorkerModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(WorkerSystem(initialContext).channel)
  }
}