package com.fracturedskies.task

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext

class TaskExecutionModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(TaskExecutionSystem(initialContext).channel)
  }
}