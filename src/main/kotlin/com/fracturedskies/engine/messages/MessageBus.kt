package com.fracturedskies.engine.messages

import kotlinx.coroutines.experimental.*

object MessageBus {
  private var messageChannels = listOf<MessageChannel>()
  fun register(messageChannel: MessageChannel): MessageChannel {
    messageChannels += messageChannel
    return messageChannel
  }
  fun unregister(messageChannel: MessageChannel) {
    messageChannels -= messageChannel
  }
  fun <T: Message> send(message: T) = async {
    messageChannels.forEach { it.send(message) }

    while (!messageChannels.all { it.isIdle() }) {
      yield()
    }
  }
}