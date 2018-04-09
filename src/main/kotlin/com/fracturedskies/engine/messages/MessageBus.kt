package com.fracturedskies.engine.messages

import kotlinx.coroutines.experimental.runBlocking
import java.util.concurrent.TimeUnit.MICROSECONDS
import java.util.concurrent.locks.LockSupport.parkNanos

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

  private fun isIdle() = messageChannels.all { it.isIdle() }

  fun waitForIdle() {
    while (!isIdle()) {
      parkNanos(MICROSECONDS.toNanos(125L))
    }
  }
}