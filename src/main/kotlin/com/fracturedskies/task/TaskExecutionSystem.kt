package com.fracturedskies.task

import com.fracturedskies.api.*
import com.fracturedskies.api.task.BehaviorStatus
import com.fracturedskies.api.task.BehaviorStatus.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.api.Update
import javax.enterprise.event.*
import javax.inject.*


@Singleton
class TaskExecutionSystem {

  @Inject
  lateinit var world: World

  @Inject
  lateinit var events: Event<Any>

  private val behavior = mutableMapOf<Id, Iterator<BehaviorStatus>>()

  fun onColonistSelectedTask(@Observes message: ColonistTaskSelected) {
    val colonist = world.colonists[message.colonistId]!!
    if (message.taskId == null) {
      behavior.remove(message.colonistId)
    } else {
      val selectedTask = world.tasks[message.taskId]!!
      behavior[colonist.id] = selectedTask.details.behavior.execute(world, colonist).iterator()
    }
  }

  fun onUpdate(@Observes update: Update) {
    behavior.entries.map { (colonistId, behavior) ->
      if (behavior.hasNext()) {
        val taskId = world.colonists[colonistId]!!.assignedTask
        val status = behavior.next()
        when (status) {
          SUCCESS -> {
            if (taskId != null) {
              world.completeTask(colonistId, taskId, update.cause)
            }
          }
          FAILURE -> {
            if (taskId != null) {
              world.rejectTask(colonistId, taskId, update.cause)
            }
          }
          RUNNING -> {
          }
        }
      }
    }
  }
}
