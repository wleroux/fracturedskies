package com.fracturedskies.worldgenerator

import com.fracturedskies.api.block.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import java.util.*
import kotlin.math.absoluteValue


class BasicTreePopulator(private val random: Random): Populator {
  override fun populate(blocks: ObjectMutableSpace<Block>, biomes: ObjectArea<Biome>) {
    (0 until blocks.width).forEach { x ->
      (0 until blocks.depth).forEach { z ->
        if (ForestBiome === biomes[x, z]) {
          if (random.nextFloat() > 0.95f) {
            val pos = highestBlock(blocks, x, z)
            if (pos != null) placeTree(blocks, pos)
          }
        }
      }
    }
  }

  private fun placeTree(blocks: ObjectMutableSpace<Block>, pos: Vector3i) {
    if (blocks[pos].type != BlockTypeDirt && blocks[pos].type != BlockTypeGrass) return
    val height = 2 + random.nextInt(8)
    val leaveStartHeight = 1 + random.nextInt(height - 1)
    val blockPlacements = mutableMapOf<Vector3i, BlockType>()
    (0 until height).forEach { y ->
      blockPlacements[Vector3i(0, y, 0)] = BlockTypeWood
    }
    blockPlacements[Vector3i(0, height, 0)] = BlockTypeLeaves
    blockPlacements[Vector3i(0, height + 1, 0)] = BlockTypeLeaves
    (-1..1).forEach { dx ->
      (-1..1).forEach { dz ->
        (leaveStartHeight..height).forEach { dy ->
          if (dx.absoluteValue == 1 && dz.absoluteValue == 1) {
            if (random.nextBoolean()) {
              blockPlacements.putIfAbsent(Vector3i(dx, dy, dz), BlockTypeLeaves)
            }
          } else {
            blockPlacements.putIfAbsent(Vector3i(dx, dy, dz), BlockTypeLeaves)
          }
        }
      }
    }

    // Try placing tree
    if (!blockPlacements.keys.map { pos + Vector3i.AXIS_Y + it }.all { blocks.has(it) && blocks[it].type == BlockTypeAir }) return
    blockPlacements.forEach { deltaPos, blockType ->
      blocks[pos + Vector3i.AXIS_Y + deltaPos] = Block(blockType)
    }
  }

  private fun highestBlock(blocks: ObjectMutableSpace<Block>, x: Int, z: Int): Vector3i? {
    val y = (0 until blocks.height).reversed().firstOrNull { y -> blocks[x, y, z].type != BlockTypeAir }
    return y?.let { Vector3i(x, y, z) }
  }
}