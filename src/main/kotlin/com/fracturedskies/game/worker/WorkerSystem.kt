package com.fracturedskies.game.worker

import com.fracturedskies.engine.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.*
import com.fracturedskies.game.BlockType
import com.fracturedskies.game.messages.*
import kotlin.coroutines.experimental.CoroutineContext


class WorkerSystem(coroutineContext: CoroutineContext, dimension: Dimension) {
  private val workers = mutableMapOf<Id, Worker>()
  private val blocks = ObjectMap(dimension) {BlockType.AIR}

  val channel = MessageChannel(coroutineContext) { message ->
    when (message) {
      is UpdateBlock -> message.updates.forEach { pos, blockType -> blocks[pos] = blockType }
      is SpawnWorker -> {
        workers += message.id to Worker(message.initialPos)
      }
      is Update -> {
        val movements = workers.map {(id, worker) ->
          val movement = Vector3i.XZ_PLANE_NEIGHBORS
              .map { worker.pos + it }
              .mapNotNull {
                val above = it + Vector3i.AXIS_Y
                val below = it + Vector3i.AXIS_NEG_Y
                when {
                  isBlocked(it) && !isBlocked(above) -> above
                  !isBlocked(it) && !isBlocked(below) -> below
                  !isBlocked(it) -> it
                  else -> null
                }
              }
              .shuffled()
              .firstOrNull()
          if (movement == null) null else id to movement
        }.filterNotNull().toMap()
        movements.forEach { id, nextPosition ->
          workers[id]!!.pos = nextPosition
        }
        MessageBus.send(MoveWorkers(movements, Cause.of(this@WorkerSystem, message.cause), Context() ))
      }
    }
  }

  private fun isBlocked(pos: Vector3i) = !blocks.has(pos) or blocks[pos].opaque
}

data class Worker(var pos: Vector3i)