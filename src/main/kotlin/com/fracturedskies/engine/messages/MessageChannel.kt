package com.fracturedskies.engine.messages

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlin.coroutines.experimental.*

class MessageChannel(context: CoroutineContext = EmptyCoroutineContext, private val handler: suspend (Message) -> Unit) {
  private val channel = Channel<Message>(Channel.UNLIMITED)
  private var processing = false
  init {
    launch(context) {
      while (isActive && !channel.isClosedForReceive) {
        if (!channel.isEmpty) {
          processing = true
          try {
            handler(channel.receive())
          } finally {
            processing = false
          }
        } else {
          yield()
        }
      }
    }
  }
  suspend fun send(message: Message) = channel.send(message)
  fun isIdle(): Boolean = channel.isEmpty && !processing
}