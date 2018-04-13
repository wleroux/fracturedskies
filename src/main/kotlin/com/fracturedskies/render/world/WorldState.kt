package com.fracturedskies.render.world

import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.ChunkSpace
import com.fracturedskies.engine.math.Vector3i

data class WorldState(
  val blocks: ChunkSpace<Block>,
  val colonists: Map<Id, Colonist>,
  val items: Map<Id, Item>,
  val timeOfDay: Float
) {
  data class Item(val id: Id, val position: Vector3i, val blockType: BlockType)
  data class Colonist(val id: Id, val pos: Vector3i)
  data class Block(val type: BlockType = AIR, val skyLight: Int = 0, val blockLight: Int = 0, val waterLevel: Byte = 0.toByte()) {
    override fun toString(): String {
      return "$type"
    }
  }

  fun process(message: Any) = when(message) {
    is BlockUpdated -> this.copy(blocks = blocks.mutate {
      message.updates.forEach { pos, value ->
        set(pos, get(pos).copy(type = value))
      }
    })
    is SkyLightUpdated -> this.copy(blocks = blocks.mutate {
      message.updates.forEach { pos, value ->
        set(pos, get(pos).copy(skyLight = value))
      }
    })
    is BlockLightUpdated -> this.copy(blocks = blocks.mutate {
      message.updates.forEach { pos, value ->
        set(pos, get(pos).copy(blockLight = value))
      }
    })
    is BlockWaterLevelUpdated -> this.copy(blocks = blocks.mutate {
      message.updates.forEach { pos, value ->
        set(pos, get(pos).copy(waterLevel = value))
      }
    })
    is ColonistSpawned -> this.copy(colonists = colonists.toMutableMap().apply {
      set(message.id, com.fracturedskies.render.world.WorldState.Colonist(message.id, message.initialPos))
    })
    is ColonistMoved -> this.copy(colonists = colonists.toMutableMap().apply {
      set(message.id, get(message.id)!!.copy(pos = message.pos))
    })
    is ItemSpawned -> this.copy(items = items.toMutableMap().apply {
      set(message.id, Item(message.id, message.position, message.blockType))
    })
    is ItemMoved -> this.copy(items = items.toMutableMap().apply {
      set(message.id, get(message.id)!!.copy(position = message.position))
    })
    is TimeUpdated -> this.copy(timeOfDay = message.time)
    else -> this
  }
}
