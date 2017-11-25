package com.fracturedskies.game.workers

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay

class Worker {
  val personalWork = mutableListOf<Work>()
  var currentWork: Work? = null

  fun receive(work: Work) {
    currentWork = work

    async {
      delay(1000L)
      work.invoke()
      currentWork = null
    }
  }

  fun isBusy() = (currentWork != null)
  companion object {
    private val HIGHEST_PRIORITY: Int = 1
    private val LOWEST_PRIORITY: Int = 10
  }
  private val preferences = mutableMapOf<WorkType, Int>()
  fun prioritize(work: List<Work>): List<Work> {
    return work
            .sortedWith(compareBy(
                    { preferences.getOrDefault(it.type, LOWEST_PRIORITY) },
                    { it.type.ordinal },
                    { it.priority }
            ))
  }
}