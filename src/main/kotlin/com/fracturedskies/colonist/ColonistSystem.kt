package com.fracturedskies.colonist

import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.send
import kotlin.coroutines.experimental.CoroutineContext


class ColonistSystem(context: CoroutineContext) {
  private var taskDetails = mapOf<Id, Any?>()
  private var colonistWork = mapOf<Id, Id>()
  val channel = MessageChannel(context) { message ->
    when (message) {
      is TaskCreated<*> -> {
        taskDetails += message.id to message.taskDetails
      }
      is TaskCompleted -> {
        taskDetails = taskDetails.filterNot { it.key == message.id }
      }
      is ColonistTaskSelected -> {
        if (message.task == null) {
          colonistWork -= message.colonist
        } else {
          colonistWork += message.colonist to message.task
        }
      }
      is Update -> {
        colonistWork.forEach { _, task ->
          val details = taskDetails[task]
          when (details) {
            is PlaceBlock -> {
              val blockPos = details.pos
              send(BlockUpdated(mapOf(blockPos to details.blockType), Cause.of(message.cause, this@ColonistSystem), message.context))
              send(TaskCompleted(task, Cause.of(message.cause, this@ColonistSystem), message.context))
            }
            is RemoveBlock -> {
              val blockPos = details.pos
              send(BlockUpdated(mapOf(blockPos to AIR), Cause.of(message.cause, this@ColonistSystem), message.context))
              send(TaskCompleted(task, Cause.of(message.cause, this@ColonistSystem), message.context))
            }
            else -> {
              send(TaskCompleted(task, Cause.of(message.cause, this@ColonistSystem), message.context))
            }
          }
        }
      }
    }
  }
}