package com.fracturedskies.engine

import com.fracturedskies.engine.messages.Message
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield
import kotlin.coroutines.experimental.CoroutineContext

abstract class GameSystem(coroutineContext: CoroutineContext = DefaultDispatcher) {
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
            this@GameSystem(event)
          } finally {
            processing = false
          }
        }
      }
    }
  }
  suspend fun send(message: Message) {
    channel.send(message)
  }
  fun isIdle(): Boolean = !processing && channel.isEmpty

  override fun toString(): String {
    return this.javaClass.simpleName
  }

  abstract suspend operator fun invoke(message: Message)
}