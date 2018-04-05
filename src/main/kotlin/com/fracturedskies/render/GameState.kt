package com.fracturedskies.render

import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.render.components.world.*

data class GameState(
    val initialized: Boolean = false,
    val world: ChunkSpace<Block>? = null,
    val workers: Map<Id, Worker>? = null,
    val timeOfDay: Float = 0f
)

fun updateGameState(state: GameState, message: Any): GameState {
  return when (message) {
    is NewGameRequested -> state.copy(
          initialized = true,
          world = ChunkSpace(message.dimension, { ObjectSpace(CHUNK_DIMENSION, { Block(AIR, 0, 0, 0) } ) } ),
          workers = emptyMap(),
          timeOfDay = 0f
    )
    is BlockUpdated -> state.copy(world = state.world!!.mutate {
      message.updates.forEach { pos, value ->
        set(pos, get(pos).copy(type = value))
      }
    })
    is SkyLightUpdated -> state.copy(world = state.world!!.mutate {
      message.updates.forEach { pos, value ->
        set(pos, get(pos).copy(skyLight = value))
      }
    })
    is BlockLightUpdated -> state.copy(world = state.world!!.mutate {
      message.updates.forEach { pos, value ->
        set(pos, get(pos).copy(blockLight = value))
      }
    })
    is BlockWaterLevelUpdated -> state.copy(world = state.world!!.mutate {
      message.updates.forEach { pos, value ->
        set(pos, get(pos).copy(waterLevel = value))
      }
    })
    is ColonistSpawned -> state.copy(workers = state.workers!!.toMutableMap().apply {
      set(message.id, Worker(message.initialPos))
    })
    is ColonistMoved -> state.copy(workers = state.workers!!.toMutableMap().apply {
      message.movements.forEach { id, nextPos ->
        set(id, get(id)!!.copy(pos = nextPos))
      }
    })
    is TimeUpdated -> state.copy(
        timeOfDay = message.time
    )
    else -> state
  }
}