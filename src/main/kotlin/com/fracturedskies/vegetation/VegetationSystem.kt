package com.fracturedskies.vegetation

import com.fracturedskies.api.*
import com.fracturedskies.api.block.*
import com.fracturedskies.engine.Id
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
    // Check for vegetation that should be broken
    val blockBreaks = vegetation
        .filter { pos ->
          val growth = world.blocks[pos][Growth::class]!!
          world.bottom(pos)?.let { world.blocks[it] }
              ?.let { !growth.isProperSoil(it) } ?: true
        }
    if (blockBreaks.isNotEmpty()) {
      blockBreaks.forEach { pos ->
        world.blocks[pos].type.itemDrop?.let { itemType ->
          world.spawnItem(Id(), itemType, pos, Cause.of(this))
        }
      }
      val blockBreakUpdates = blockBreaks.map { it to Block(BlockTypeAir) }.toMap()
      world.updateBlocks(blockBreakUpdates, Cause.of(this))
    }

    // Check for vegetation that should be updated
    val blockGrowthUpdates = vegetation
        .associate { pos -> pos to world.blocks[pos][Growth::class]!! }
        .filter { (_, growth) -> growth.probability > random.nextFloat() }
        .mapValues { (_, growth) -> growth.block() }
    if (blockGrowthUpdates.isNotEmpty()) {
      world.updateBlocks(blockGrowthUpdates, Cause.of(this))
    }
  }
}