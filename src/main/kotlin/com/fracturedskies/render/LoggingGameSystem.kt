package com.fracturedskies.render

import com.fracturedskies.engine.GameSystem
import com.fracturedskies.engine.Render
import com.fracturedskies.engine.Update
import com.fracturedskies.engine.messages.Message
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlin.coroutines.experimental.CoroutineContext

class LoggingGameSystem(coroutineContext: CoroutineContext = DefaultDispatcher): GameSystem(coroutineContext) {
  override suspend fun invoke(message: Message) {
    if (message is Update || message is Render) return
    println(message)
  }
}
