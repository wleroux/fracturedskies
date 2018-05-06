package com.fracturedskies.task

import com.fracturedskies.api.World
import com.fracturedskies.api.entity.Colonist
import com.fracturedskies.api.task.*
import com.fracturedskies.engine.api.Update
import javax.enterprise.event.*
import javax.inject.Inject

/**
 * This system is responsible for assigning the colonist to the desired task every tick
 */
class TaskManagementSystem {

  @Inject
  lateinit var events: Event<Any>

  @Inject
  lateinit var world: World

  fun onUpdate(@Observes message: Update) {
    world.colonists.values
        .map { colonist -> colonist to getDesiredTask(world, colonist, world.tasks.values) }
        .groupBy({ (_, desiredTask) -> desiredTask }, { (colonist, _) -> colonist })
        .mapValues { entry ->
          when {
            entry.key != null && entry.value.size > 1 ->
              listOf(entry.value.minBy { colonist -> entry.key?.details?.behavior?.cost(world, colonist) ?: 0 }!!)
            else -> entry.value
          }
        }
        .forEach { task, colonists ->
          colonists.forEach { colonist ->
            if (colonist.assignedTask != task?.id) {
              world.selectTask(colonist.id, task?.id, message.cause)
            }
          }
        }
  }

  private fun getDesiredTask(world: World, colonist: Colonist, tasks: Collection<Task>): Task? {
    val comparator = colonistPriorityComparator(world)

    var desiredTask: Task? = null
    for (task in tasks.shuffled()) {
      if (!task.details.condition.matches(world, colonist, task)) continue
      if (desiredTask != null && comparator.compare(colonist, desiredTask, task) >= 0) continue
      if (!task.details.behavior.isPossible(world, colonist)) continue
      desiredTask = task
    }
    return desiredTask
  }
}