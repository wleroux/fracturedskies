package com.fracturedskies.render

import com.fracturedskies.api.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import javax.enterprise.event.Observes
import javax.inject.Singleton

@Singleton
class DirtyFlags{
  var blocksDirty: BooleanMutableSpace = BooleanMutableSpace(Dimension(0, 0, 0))
    private set
  var colonistsDirty: Boolean = false
    private set

  private fun chunks(pos: Vector3i) = (Vector3i.NEIGHBOURS + Vector3i.ADDITIVE_UNIT)
      .asSequence()
      .map(pos::plus)
      .map(this::chunkPos)
      .distinct()
      .filter(blocksDirty::has)

  private fun chunkPos(pos: Vector3i) = (pos / CHUNK_DIMENSION)

  fun clear() {
    blocksDirty.clear()
    colonistsDirty = false
  }

  fun onNewGameRequested(@Observes newGameRequested: NewGameRequested) {
    blocksDirty = BooleanMutableSpace(newGameRequested.dimension, { true })
    colonistsDirty = true
  }

  fun onWorldGenerated(@Observes worldGenerated: WorldGenerated) {
    worldGenerated.blocks
        .flatMap { (position, _) -> chunks(position) }
        .forEach { blocksDirty[it] = true }
  }

  fun onBlocksUpdated(@Observes blocksUpdated: BlocksUpdated) {
    blocksUpdated.blocks
        .asSequence()
        .flatMap { (position, _, _) -> chunks(position) }
        .forEach { blocksDirty[it] = true }
  }

  fun onColonistSpawned(@Observes colonistSpawned: ColonistSpawned) {
    colonistsDirty = true
  }
}
