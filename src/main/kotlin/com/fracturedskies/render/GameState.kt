package com.fracturedskies.render

import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.render.world.*
import com.fracturedskies.task.Item

data class GameState(
    val initialized: Boolean = false,
    val world: ChunkSpace<Block>? = null,
    val workers: Map<Id, Worker>? = null,
    val items: Map<Id, Item>? = null,
    val timeOfDay: Float = 0f
) {
  fun process(message: Any): GameState {
    return when (message) {
      is NewGameRequested -> this.copy(
            initialized = true,
            world = ChunkSpace(message.dimension, { ObjectSpace(CHUNK_DIMENSION, { Block(AIR, 0, 0, 0) } ) } ),
            workers = emptyMap(),
            items = emptyMap(),
            timeOfDay = 0f
      )
      is BlockUpdated -> this.copy(world = world!!.mutate {
        message.updates.forEach { pos, value ->
          set(pos, get(pos).copy(type = value))
        }
      })
      is SkyLightUpdated -> this.copy(world = world!!.mutate {
        message.updates.forEach { pos, value ->
          set(pos, get(pos).copy(skyLight = value))
        }
      })
      is BlockLightUpdated -> this.copy(world = world!!.mutate {
        message.updates.forEach { pos, value ->
          set(pos, get(pos).copy(blockLight = value))
        }
      })
      is BlockWaterLevelUpdated -> this.copy(world = world!!.mutate {
        message.updates.forEach { pos, value ->
          set(pos, get(pos).copy(waterLevel = value))
        }
      })
      is ColonistSpawned -> this.copy(workers = workers!!.toMutableMap().apply {
        set(message.id, Worker(message.initialPos))
      })
      is ColonistMoved -> this.copy(workers = workers!!.toMutableMap().apply {
          set(message.id, get(message.id)!!.copy(pos = message.pos))
      })
      is ItemSpawned -> this.copy(items = items!!.toMutableMap().apply {
        set(message.id, Item(message.id, message.blockType, message.position))
      })
      is ItemMoved -> this.copy(items = items!!.toMutableMap().apply {
        set(message.id, Item(message.id, get(message.id)!!.blockType, message.position))
      })
      is TimeUpdated -> this.copy(timeOfDay = message.time)
      else -> this
    }
  }
}