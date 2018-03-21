package com.fracturedskies.time

import com.fracturedskies.api.TimeUpdated
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.messages.*
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
        MessageBus.send(TimeUpdated((time % TIME_PER_DAY) / TIME_PER_DAY, Cause.of(this@TimeSystem), MultiTypeMap()))
      }
    }
  }
}