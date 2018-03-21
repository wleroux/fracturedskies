package com.fracturedskies

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.api.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageChannel
import kotlin.coroutines.experimental.CoroutineContext


class MessageLoggerModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(MessageChannel(initialContext) { message ->
      when (message) {
        is Render -> {}
        is Update -> {}
        else -> {
          println(message)
        }
      }
    })
  }
}