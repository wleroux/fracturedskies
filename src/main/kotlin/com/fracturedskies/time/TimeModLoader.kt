package com.fracturedskies.time

import com.fracturedskies.api.TimeUpdated
import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext

const val TIME_PER_DAY = 60f

class TimeModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    var time = 3 * TIME_PER_DAY / 2
    register(MessageChannel(initialContext) { message ->
      if (message is Update) {
        time += message.dt
        MessageBus.send(TimeUpdated((time % TIME_PER_DAY) / TIME_PER_DAY, message.cause, message.context))
      }
    })
  }
}