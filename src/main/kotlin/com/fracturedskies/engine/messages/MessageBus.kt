package com.fracturedskies.engine.messages

import com.fracturedskies.engine.GameSystem
import kotlinx.coroutines.experimental.yield

object MessageBus {
  private val messageChannels = mutableListOf<MessageChannel>()
  fun subscribe(gameSystem: GameSystem) {
    subscribe(gameSystem.messageChannel)
  }
  fun unsubscribe(gameSystem: GameSystem) {
    unsubscribe(gameSystem.messageChannel)
  }

  fun subscribe(messageChannel: MessageChannel): MessageChannel {
    messageChannels.add(messageChannel)
    return messageChannel
  }
  fun unsubscribe(messageChannel: MessageChannel) {
    messageChannels.remove(messageChannel)
  }

  suspend fun <T: Message> publish(message: T) {
    messageChannels.forEach { it.send(message) }
  }
  suspend fun <T: Message> publishAndWait(message: T) {
    publish(message)
    while (!isIdle()) {
      yield()
    }
  }
  private fun isIdle() = messageChannels.all {it.isIdle()}
}