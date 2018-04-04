package com.fracturedskies.engine.messages

import kotlinx.coroutines.experimental.*
import java.util.concurrent.TimeUnit.MILLISECONDS

object MessageBus {
  private var messageChannels = listOf<MessageChannel>()
  fun register(messageChannel: MessageChannel): MessageChannel {
    messageChannels += messageChannel
    return messageChannel
  }
  fun unregister(messageChannel: MessageChannel) {
    messageChannels -= messageChannel
  }
  fun <T: Message> send(message: T) = runBlocking {
    messageChannels.forEach { it.send(message) }
  }

  fun isIdle() = messageChannels.all { it.isIdle() }

  suspend fun waitForIdle() {
    while (!isIdle()) {
      delay(1L, MILLISECONDS)
    }
  }
}