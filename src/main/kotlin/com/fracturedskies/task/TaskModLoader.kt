package com.fracturedskies.task

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext

class TaskModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(TaskSystem(initialContext).channel)
  }
}