package com.fracturedskies.task

import com.fracturedskies.api.*
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.collections.Dimension
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.task.api.colonistPriorityComparator
import kotlinx.coroutines.experimental.async
import kotlin.coroutines.experimental.CoroutineContext

class TaskManagementState(dimension: Dimension): WorldState(dimension)

/**
 * This system is responsible for assigning the colonist to the desired task every tick
 */
class TaskManagementSystem(context: CoroutineContext) {
  lateinit var state: TaskManagementState
  var initialized = false
  val channel = MessageChannel(context) { message ->
    when (message) {
      is NewGameRequested -> {
        state = TaskManagementState(message.dimension)
        initialized = true
      }
      is Update -> {
        state.colonists.values
            .map { colonist -> async(context) { colonist to getDesiredTask(state, colonist, state.tasks.values) } }.map { it.await() }
            .groupBy({(_, desiredTask) -> desiredTask}, {(colonist, _) -> colonist})
            .mapValues { entry ->
              when {
                entry.key != null && entry.value.size > 1 ->
                  listOf(entry.value.minBy { colonist -> entry.key?.behavior?.cost(state, colonist) ?: 0 }!!)
                else -> entry.value
              }
            }
            .forEach { task, colonists -> colonists.forEach { colonist ->
              if (colonist.assignedTask != task?.id) {
                send(ColonistTaskSelected(colonist.id, task?.id, message.cause, message.context))
              }
            } }
      }
      else -> if (initialized) state.process(message)
    }
  }
}

fun getDesiredTask(state: WorldState, colonist: Colonist, tasks: Collection<Task<*>>): Task<*>? {
  val comparator = colonistPriorityComparator(state)

  var desiredTask: Task<*>? = null
  for (task in tasks) {
    if (!task.condition.matches(colonist, task))
      continue

    if (desiredTask == null) {
      desiredTask = task
    } else {
      if (comparator.compare(colonist, desiredTask, task) < 0)
        desiredTask = task
    }
  }
  return desiredTask
}
