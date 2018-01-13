package com.fracturedskies.game.time

import com.fracturedskies.engine.Update
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.messages.*
import com.fracturedskies.game.messages.TimeUpdated
import kotlin.coroutines.experimental.CoroutineContext

class TimeSystem(coroutineContext: CoroutineContext) {
  companion object {
    val TIME_PER_DAY = 60f
  }
  private var time = 3 * TIME_PER_DAY / 2
  val channel = MessageChannel(coroutineContext) { message ->
    when (message) {
      is Update -> {
        time += message.dt
        MessageBus.send(TimeUpdated((time % TIME_PER_DAY) / TIME_PER_DAY, Cause.of(this@TimeSystem), Context()))
      }
    }
  }
}