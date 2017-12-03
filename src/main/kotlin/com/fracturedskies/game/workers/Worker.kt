package com.fracturedskies.game.workers

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.game.messages.UpdateBlock
import com.fracturedskies.game.messages.UpdateBlockWork
import com.fracturedskies.game.messages.Work
import com.fracturedskies.game.messages.WorkType
import kotlinx.coroutines.experimental.async

class Worker {
  val personalWork = mutableListOf<Work>()
  private var currentWork: Work? = null

  fun receive(work: Work) {
    currentWork = work

    async {
      when (work) {
        is UpdateBlockWork -> {
          send(UpdateBlock(Vector3i(work.x, work.y, work.z), work.blockType, Cause.of(this), Context()))
        }
      }
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