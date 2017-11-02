package com.fracturedskies.engine.messages

import com.fracturedskies.engine.GameSystem
import kotlinx.coroutines.experimental.yield

object MessageBus {
  private val gameSystems = mutableListOf<GameSystem>()
  fun subscribe(gameSystem: GameSystem) {
    gameSystems.add(gameSystem)
  }
  suspend fun <T: Message> publish(message: T) {
    gameSystems.forEach { it.send(message) }
  }
  suspend fun <T: Message> publishAndWait(message: T) {
    publish(message)
    while (!isIdle()) {
      yield()
    }
  }

  private fun isIdle() = gameSystems.all {it.isIdle()}
}