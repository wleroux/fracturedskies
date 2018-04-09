package com.fracturedskies.task

import com.fracturedskies.api.*
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.task.behavior.BehaviorStatus.*
import kotlinx.coroutines.experimental.async
import kotlin.coroutines.experimental.CoroutineContext


class TaskExecutionSystem(context: CoroutineContext) {
  var initialized = false
  lateinit var state: TaskExecutionState
  val channel = MessageChannel(context) { message ->
    when (message) {
      is NewGameRequested -> {
        state = TaskExecutionState(message.dimension)
        initialized = true
      }
      is Update -> {
        state.behavior.entries.map { (colonist, behavior) ->
          async(context) {
            if (behavior.hasNext()) {
              val task = state.colonists[colonist]!!.assignedTask
              val status = behavior.next()
              when (status) {
                SUCCESS -> if (task != null) send(TaskCompleted(task, message.cause, message.context))
                FAILURE -> if (task != null) send(ColonistRejectedTask(colonist, task, message.cause, message.context))
                RUNNING -> { }
              }
            }
          }
        }.forEach { it.await() }
      }
      else -> if (initialized) state.process(message)
    }
  }
}
