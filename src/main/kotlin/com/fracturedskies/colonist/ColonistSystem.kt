package com.fracturedskies.colonist

import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.send
import kotlin.coroutines.experimental.CoroutineContext


class ColonistSystem(context: CoroutineContext) {
  var initialized = false
  lateinit var state: ColonistMap
  lateinit var pathFinder: PathFinder
  val channel = MessageChannel(context) { message ->
    when (message) {
      is NewGameRequested -> {
        state = ColonistMap(message.dimension)
        pathFinder = PathFinder(state.blocked)
        initialized = true
      }
      is BlockUpdated -> message.updates.forEach { pos, blockType -> state.blocked[pos] = blockType.opaque }
      is TaskCreated<*> -> state.taskDetails[message.id] = message.taskDetails
      is TaskCancelled -> state.taskDetails[message.id] = null
      is TaskCompleted -> state.taskDetails[message.id] = null
      is ColonistSpawned -> state.colonistPositions[message.id] = message.initialPos
      is ColonistMoved -> state.colonistPositions[message.id] = message.pos
      is ColonistTaskSelected -> state.colonistWork[message.colonist] = message.task
      is Update -> {
        state.colonistWork.forEach { colonist, task ->
          if (task != null) {
            when (state.taskDetails[task]) {
              is PlaceBlock -> executePlaceBlock(colonist, task)
              is RemoveBlock -> executeRemoveBlock(colonist, task)
              else -> executeMissingExecutor(colonist, task)
            }
          }
        }
      }
    }
  }

  private fun executePlaceBlock(colonist: Id, task: Id) {
    val taskDetails = state.taskDetails[task] as PlaceBlock
    val blockPos = taskDetails.pos
    val colonistPos = state.colonistPositions[colonist]!!
    val pathToBlock = pathFinder.find(colonistPos, taskDetails.pos)
    when {
      colonistPos == blockPos -> {
        val sideStep = Vector3i.NEIGHBOURS.map { colonistPos + it }
            .filterNot { state.blocked[it] }
            .firstOrNull()
        if (sideStep != null) {
          send(ColonistMoved(colonist, sideStep, Cause.of(this)))
        }
      }
      pathToBlock.size == 1 -> {
        send(BlockUpdated(mapOf(taskDetails.pos to taskDetails.blockType), Cause.of(this)))
        send(TaskCompleted(task, Cause.of(this)))
      }
      pathToBlock.isEmpty() -> {
        send(TaskCancelled(task, Cause.of(this)))
      }
      else -> send(ColonistMoved(colonist, pathToBlock[0], Cause.of(this)))
    }
  }

  private fun executeRemoveBlock(colonist: Id, task: Id) {
    val taskDetails = state.taskDetails[task] as RemoveBlock
    val blockPos = taskDetails.pos
    val colonistPos = state.colonistPositions[colonist]!!
    val isNeighbor = Vector3i.NEIGHBOURS.map({ it + colonistPos }).any({ it == blockPos })
    if (isNeighbor) {
      send(BlockUpdated(mapOf(taskDetails.pos to AIR), Cause.of(this)))
      send(TaskCompleted(task, Cause.of(this)))
    } else {
      val pathToBlock =
          Vector3i.NEIGHBOURS
              .map { it + blockPos }
              .filterNot { state.blocked[it] }
              .map { pathFinder.find(colonistPos, it) }
              .filterNot { it.isEmpty() }
              .minBy { it.size }
              ?: emptyList()
      if (pathToBlock.isEmpty()) {
        send(TaskCancelled(task, Cause.of(this)))
      } else {
        send(ColonistMoved(colonist, pathToBlock[0], Cause.of(this)))
      }
    }
  }

  private fun executeMissingExecutor(colonist: Id, task: Id) {
    send(TaskCancelled(task, Cause.of(this)))
  }
}