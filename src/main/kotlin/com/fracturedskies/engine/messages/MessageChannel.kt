package com.fracturedskies.engine.messages

import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

class MessageChannel(coroutineContext: CoroutineContext = EmptyCoroutineContext, private val handler: suspend (Message) -> Unit) {
  private val channel = Channel<Message>(Channel.UNLIMITED)
  private var processing = false
  init {
    launch(coroutineContext) {
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