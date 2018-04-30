package com.fracturedskies.render

import com.fracturedskies.api.*
import com.fracturedskies.engine.api.Message
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import javax.enterprise.event.Observes
import javax.inject.Singleton

@Singleton
class DirtyFlags{
  var blocksDirty: BooleanMutableSpace = BooleanMutableSpace(Dimension(0, 0, 0))
    private set
  var itemsDirty: Boolean = false
    private set
  var colonistsDirty: Boolean = false
    private set

  private fun chunks(pos: Vector3i) = (Vector3i.NEIGHBOURS + Vector3i.ADDITIVE_UNIT)
      .map(pos::plus)
      .map(this::chunkPos)
      .toSet()
      .filter(blocksDirty::has)

  private fun chunkPos(pos: Vector3i) = (pos / CHUNK_DIMENSION)

  fun clear() {
    blocksDirty.clear()
    itemsDirty = false
    colonistsDirty = false
  }

  fun process(@Observes message: Message) {
    when (message) {
      is NewGameRequested -> {
        blocksDirty = BooleanMutableSpace(message.dimension, { true })
        itemsDirty = true
        colonistsDirty = true
      }
      is WorldGenerated -> {
        val blocks = message.blocks
        blocks
            .flatMap { chunks(it.key) }
            .forEach { blocksDirty[it] = true }
      }
      is BlocksUpdated -> {
        val blocks = message.blocks
        blocks
            .flatMap { chunks(it.key) }
            .forEach { blocksDirty[it] = true }
      }
      is ColonistSpawned -> colonistsDirty = true
      is ItemSpawned -> itemsDirty = true
    }
  }
}
