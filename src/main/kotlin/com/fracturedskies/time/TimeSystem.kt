package com.fracturedskies.time

import com.fracturedskies.api.World
import com.fracturedskies.engine.api.Update
import java.util.concurrent.TimeUnit.*
import javax.enterprise.event.*
import javax.inject.*

private val TICKS_PER_DAY = 60 * SECONDS.convert(15, MINUTES)

@Singleton
class TimeSystem {

  @Inject
  lateinit var events: Event<Any>

  @Inject
  lateinit var world: World

  var time = TICKS_PER_DAY / 3

  fun onUpdate(@Observes message: Update) {
    time ++
    world.updateTimeOfDay((time % TICKS_PER_DAY).toFloat() / TICKS_PER_DAY.toFloat(), message.cause)
  }
}