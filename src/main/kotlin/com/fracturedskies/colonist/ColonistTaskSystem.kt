package com.fracturedskies.colonist

import com.fracturedskies.api.*
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.engine.messages.MessageChannel
import kotlin.coroutines.experimental.CoroutineContext

/**
 * This system is responsible for assigning the colonist to the desired task every tick
 */
class ColonistTaskSystem(context: CoroutineContext) {
  var tasks = listOf<Task<*>>()
  var colonists = listOf<Colonist>()

  val channel = MessageChannel(context) { message ->
    when (message) {
      is ColonistSpawned -> {
        colonists += Colonist(message.id, mapOf(), AllCondition, ColonistPriorityComparator)
      }
      is TaskCreated<*> -> {
        tasks += Task(message.id, message.category, message.priority, message.condition, listOf(), message.taskDetails)
      }
      is TaskCompleted -> {
        tasks = tasks.filterNot { it.id == message.id }
      }
      is Update -> {
        colonists.forEach { colonist ->
          val desiredTask = getDesiredTask(colonist, tasks)
          if (!(desiredTask != null && desiredTask.assigned.contains(colonist.id))) {
            tasks = tasks.map { task ->
              when {
                task === desiredTask && !task.assigned.contains(colonist.id) -> task.copy(assigned = (task.assigned + colonist.id))
                task !== desiredTask && task.assigned.contains(colonist.id) -> task.copy(assigned = (task.assigned - colonist.id))
                else -> task
              }
            }

            send(ColonistTaskSelected(colonist.id, desiredTask?.id, message.cause, message.context))
          }
        }
      }
    }
  }
}

fun getDesiredTask(colonist: Colonist, tasks: List<Task<*>>): Task<*>? {
  return tasks
      .filter { task -> task.condition.matches(colonist, task) }
      .filter { task -> colonist.taskCondition.matches(colonist, task) }
      .maxWith(Comparator { a, b -> colonist.taskPriorityComparator.compare(colonist, a, b) })
}
