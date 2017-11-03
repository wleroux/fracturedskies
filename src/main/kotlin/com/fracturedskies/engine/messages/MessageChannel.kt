package com.fracturedskies.engine.messages

import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield
import kotlin.coroutines.experimental.CoroutineContext

class MessageChannel(coroutineContext: CoroutineContext, private val handler: suspend (Message) -> Unit) {
  private val channel = Channel<Message>(Channel.UNLIMITED)
  private var processing = false
  init {
    launch(coroutineContext) {
      while (isActive) {
        if (channel.isEmpty) {
          yield()
        } else {
          processing = true
          try {
            val event = channel.receive()
            handler(event)
          } finally {
            processing = false
          }
        }
      }
    }
  }
  suspend fun send(message: Message) = channel.send(message)
  fun isIdle(): Boolean = !processing && channel.isEmpty
}