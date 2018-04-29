package com.fracturedskies.render

import com.fracturedskies.World
import com.fracturedskies.api.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i

data class GameState(
    var gameStarted: Boolean = false,
    var world: RenderWorld? = null
) {
  class RenderWorld(dimension: Dimension) : World(dimension) {
    val blocksDirty = BooleanMutableSpace(dimension / CHUNK_DIMENSION, { false })
    var itemsDirty = false
    var colonistsDirty = false

    private fun chunks(pos: Vector3i) = (Vector3i.NEIGHBOURS + Vector3i.ADDITIVE_UNIT)
        .map { it + pos }
        .map { it / CHUNK_DIMENSION }
        .toSet()
        .filter { blocksDirty.has(it) }

    override fun process(message: Any) {
      super.process(message)

      when (message) {
        is WorldGenerated -> message.blocks.forEach { (blockIndex, _) ->
          val blockPos = message.offset + message.blocks.vector3i(blockIndex)
          chunks(blockPos).forEach { chunkPos -> blocksDirty[chunkPos] = true }
        }
        is BlockUpdated -> message.updates.forEach { pos, _ -> chunks(pos).forEach { chunkPos -> blocksDirty[chunkPos] = true } }
        is BlockWaterLevelUpdated -> message.updates.forEach { pos, _ -> chunks(pos).forEach { chunkPos -> blocksDirty[chunkPos] = true } }
        is SkyLightUpdated -> message.updates.forEach { pos, _ -> chunks(pos).forEach { chunkPos -> blocksDirty[chunkPos] = true } }
        is BlockLightUpdated -> message.updates.forEach { pos, _ -> chunks(pos).forEach { chunkPos -> blocksDirty[chunkPos] = true } }
        is ColonistSpawned -> colonistsDirty = true
        is ItemSpawned -> itemsDirty = true
      }
    }
  }

  fun process(message: Any) {
    when (message) {
      is NewGameRequested -> {
        gameStarted = true
        world = RenderWorld(message.dimension)
      }
      else -> {
        if (gameStarted)
          world!!.process(message)
      }
    }
  }

  fun clearDirty() {
    if (gameStarted) {
      world!!.blocksDirty.clear()
      world!!.itemsDirty = false
      world!!.colonistsDirty = false
    }
  }
}