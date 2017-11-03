package com.fracturedskies.engine

import com.fracturedskies.engine.messages.Message
import com.fracturedskies.engine.messages.MessageChannel
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlin.coroutines.experimental.CoroutineContext

abstract class GameSystem(coroutineContext: CoroutineContext = DefaultDispatcher) {
  val messageChannel = MessageChannel(coroutineContext, {message -> this@GameSystem.invoke(message)})
  abstract suspend operator fun invoke(message: Message): Unit
  override fun toString(): String {
    return this.javaClass.simpleName
  }
}