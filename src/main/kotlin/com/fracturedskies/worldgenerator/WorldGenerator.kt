package com.fracturedskies.worldgenerator

import com.fracturedskies.engine.collections.Dimension
import com.fracturedskies.engine.math.*
import com.fracturedskies.BlockType
import com.fracturedskies.BlockType.*


class WorldGenerator(private val dimension: Dimension, private val seed: Int) {
  fun generate(): Map<Vector3i, BlockType> {
    val blocks = mutableMapOf<Vector3i, BlockType>()
    (0 until dimension.width).flatMap { x ->
      (0 until dimension.depth).flatMap {  z ->
        (0 until dimension.height).map { y ->
          val pos = Vector3i(x, y, z)
          pos to if (isBlock(pos)) BlockType.BLOCK else BlockType.AIR
        }
      }
    }.toMap(blocks)

    // Convert to block to grass
    (0 until dimension.width).forEach {x ->
      (0 until dimension.depth).forEach {z ->
        val pos = highestBlock(blocks, x, z)
        if (blocks[pos] != AIR)
          blocks[pos] = GRASS
      }
    }

    return blocks
  }

  private fun highestBlock(blockType: Map<Vector3i, BlockType>, x: Int, z: Int) =
    Vector3i(x, (0 until dimension.height).reversed().firstOrNull { blockType[Vector3i(x, it, z)] != AIR } ?: 0, z)

  private fun isBlock(pos: Vector3i): Boolean {
    val h = fbm_noise3(seed.toFloat() + pos.x.toFloat() / dimension.width.toFloat(), seed.toFloat() + 0f, seed.toFloat() + pos.z.toFloat() / dimension.depth.toFloat(), 2.0f, 0.5f, 6)
    return pos.y.toFloat() < map(h, -0.5f..0.5f, (dimension.height.toFloat() * (6f/8f))..(dimension.height.toFloat() * (7f/8f)))
  }
}
