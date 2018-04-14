package com.fracturedskies.task

import com.fracturedskies.WorldState
import com.fracturedskies.api.ColonistTaskSelected
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.Dimension
import com.fracturedskies.task.behavior.BehaviorStatus


class TaskExecutionState(dimension: Dimension): WorldState(dimension) {
  val behavior = mutableMapOf<Id, Iterator<BehaviorStatus>>()

  override fun process(message: Any) {
    super.process(message)
    when (message) {
      is ColonistTaskSelected -> {
        val colonist = colonists[message.colonist]!!
        if (message.task == null) {
          behavior.remove(message.colonist)
        } else {
          val selectedTask = tasks[message.task]!!
          behavior[colonist.id] = selectedTask.behavior.execute(this, colonist).iterator()
        }
      }
    }
  }
}