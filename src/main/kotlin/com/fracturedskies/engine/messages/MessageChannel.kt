package com.fracturedskies.engine.messages

import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.coroutines.experimental.*

class MessageChannel(context: CoroutineContext = EmptyCoroutineContext, private val handler: suspend (Message) -> Unit) {
  private val channel = Channel<Message>(Channel.UNLIMITED)
  private var processing = false
  init {
    launch(context) {
      val iterator = channel.iterator()
      while (iterator.hasNext()) {
        processing = true
        try {
          handler(iterator.next())
        } finally {
          processing = false
        }
      }
    }
  }
  suspend fun send(message: Message) = channel.send(message)

  fun isIdle(): Boolean = !processing && channel.isEmpty
}