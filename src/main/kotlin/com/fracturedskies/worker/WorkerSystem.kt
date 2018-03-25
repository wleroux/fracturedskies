package com.fracturedskies.worker

import com.fracturedskies.api.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.send
import kotlin.coroutines.experimental.CoroutineContext


class WorkerSystem(context: CoroutineContext) {
  private lateinit var workers: MutableMap<Id, Worker>
  private lateinit var blocks: ObjectMutableSpace<BlockType>
  private var initialized = false

  val channel = MessageChannel(context) { message ->
    when (message) {
      is NewGameRequested -> {
        blocks = ObjectMutableSpace(message.dimension) { BlockType.AIR }
        workers = mutableMapOf()
        initialized = true
      }
      is UpdateBlock -> message.updates.forEach { pos, blockType -> blocks[pos] = blockType }
      is SpawnWorker -> {
        if (!initialized) return@MessageChannel
        workers.plusAssign((message.id to Worker(message.initialPos)))
      }
      is Update -> {
        if (!initialized) return@MessageChannel
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
        send(MoveWorkers(movements, Cause.of(this@WorkerSystem, message.cause), MultiTypeMap()))
      }
    }
  }

  private fun isBlocked(pos: Vector3i) = !blocks.has(pos) || blocks[pos].opaque
}

data class Worker(var pos: Vector3i)