package com.fracturedskies.engine.messages

import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.*

class MessageChannel(context: CoroutineContext = EmptyCoroutineContext, private val handler: suspend (Message) -> Unit) {
  private val channel = Channel<Message>(Channel.UNLIMITED)
  private var processing = false
  init {
    launch(context) {
      channel.consumeEach { message ->
        try {
          processing = true
          handler(message)
        } finally {
          processing = false
        }
      }
    }
  }
  suspend fun send(message: Message) = channel.send(message)
  fun isIdle(): Boolean = channel.isEmpty && !processing
}