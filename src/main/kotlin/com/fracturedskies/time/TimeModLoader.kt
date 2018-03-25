package com.fracturedskies.time

import com.fracturedskies.api.TimeUpdated
import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.engine.messages.MessageChannel
import java.util.concurrent.TimeUnit.*
import kotlin.coroutines.experimental.CoroutineContext

private val TICKS_PER_DAY = 60 * SECONDS.convert(15, MINUTES)

class TimeModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    var time = 1 * TICKS_PER_DAY / 4
    register(MessageChannel(initialContext) { message ->
      if (message is Update) {
        time ++
        send(TimeUpdated((time % TICKS_PER_DAY).toFloat() / TICKS_PER_DAY.toFloat(), message.cause, message.context))
      }
    })
  }
}