package com.fracturedskies.engine.messages

import kotlinx.coroutines.experimental.yield

object MessageBus {
  private val messageChannels = mutableListOf<MessageChannel>()
  fun register(messageChannel: MessageChannel): MessageChannel {
    messageChannels.add(messageChannel)
    return messageChannel
  }
  fun unregister(messageChannel: MessageChannel) {
    messageChannels.remove(messageChannel)
  }

  suspend fun <T: Message> dispatch(message: T) {
    messageChannels.forEach { it.send(message) }
  }
  suspend fun <T: Message> dispatchAndWait(message: T) {
    dispatch(message)
    while (!isIdle()) {
      yield()
    }
  }
  private fun isIdle() = messageChannels.all {it.isIdle()}
}