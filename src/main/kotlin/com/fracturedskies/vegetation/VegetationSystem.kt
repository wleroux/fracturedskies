package com.fracturedskies.vegetation

import com.fracturedskies.api.*
import com.fracturedskies.engine.api.*
import com.fracturedskies.engine.collections.forEach
import com.fracturedskies.engine.math.Vector3i
import java.util.*
import javax.enterprise.event.Observes
import javax.inject.*

@Singleton
class VegetationSystem {

  private val random = Random()

  @Inject
  lateinit var world: World

  private val vegetation: MutableSet<Vector3i> = mutableSetOf()
  fun onWorldGenerated(@Observes worldGenerated: WorldGenerated) {
    worldGenerated.blocks.forEach { pos, block ->
      if (block.has(Growth::class)) {
        vegetation += pos
      } else {
        vegetation -= pos
      }
    }
  }

  fun onBlocksUpdated(@Observes blocksUpdated: BlocksUpdated) {
    blocksUpdated.blocks.forEach { pos, block ->
      if (block.has(Growth::class)) {
        vegetation += pos
      } else {
        vegetation -= pos
      }
    }
  }

  fun onUpdate(@Observes update: Update) {
    val blockUpdates = vegetation
        .associate { pos -> pos to world.blocks[pos][Growth::class]!! }
        .filter { (_, growth) -> growth.probability > random.nextFloat() }
        .mapValues { (_, growth) -> growth.block }

    if (blockUpdates.isNotEmpty()) {
      world.updateBlocks(blockUpdates, Cause.of(this))
    }
  }
}