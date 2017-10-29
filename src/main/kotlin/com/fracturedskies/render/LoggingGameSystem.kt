package com.fracturedskies.render

import com.fracturedskies.engine.GameSystem
import com.fracturedskies.engine.Render
import com.fracturedskies.engine.Update
import com.fracturedskies.engine.events.Event
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlin.coroutines.experimental.CoroutineContext

class LoggingGameSystem(coroutineContext: CoroutineContext = DefaultDispatcher): GameSystem(coroutineContext) {
  override suspend fun invoke(event: Event) {
    if (event is Update || event is Render) return
    println(event)
  }
}
