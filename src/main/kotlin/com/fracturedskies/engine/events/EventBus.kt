package com.fracturedskies.engine.events

import com.fracturedskies.engine.GameSystem
import kotlinx.coroutines.experimental.yield

object EventBus {
  private val gameSystems = mutableListOf<GameSystem>()
  fun subscribe(gameSystem: GameSystem) {
    gameSystems.add(gameSystem)
  }
  suspend fun <T: Event> publish(event: T) {
    gameSystems.forEach { it.send(event) }
  }
  suspend fun <T: Event> publishAndWait(event: T) {
    publish(event)
    while (!isIdle()) {
      yield()
    }
  }

  private fun isIdle() = gameSystems.all {it.isIdle()}
}