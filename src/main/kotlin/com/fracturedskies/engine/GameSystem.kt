package com.fracturedskies.engine

import com.fracturedskies.engine.events.Event
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield
import kotlin.coroutines.experimental.CoroutineContext

abstract class GameSystem(coroutineContext: CoroutineContext = DefaultDispatcher) {
  private val channel = Channel<Event>(Channel.UNLIMITED)
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
  suspend fun send(event: Event) {
    channel.send(event)
  }
  fun isIdle(): Boolean = !processing && channel.isEmpty
  abstract suspend operator fun invoke(event: Event)
}