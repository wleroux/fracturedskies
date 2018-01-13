package com.fracturedskies.game.worldgenerator

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.game.BlockType
import com.fracturedskies.game.BlockType.*


class WorldGenerator(private val dimension: Dimension, private val seed: Int): (Int) -> BlockType {
  fun generate(): ObjectMap<BlockType> {
    return ObjectMap(dimension, this).also { world ->
      (0 until dimension.width).forEach {x ->
        (0 until dimension.depth).forEach {z ->
          val highestBlock = highestBlock(world, x, z)
          world[x, highestBlock, z] = GRASS
        }
      }
    }
  }

  private fun highestBlock(world: ObjectMap<BlockType>, x: Int, z: Int) =
    (0 until world.height).reversed().firstOrNull { world[x, it, z] != AIR } ?: 0

  override operator fun invoke(pos: Int): BlockType {
    val (x, y, z) = dimension.toVector3i(pos)
    return if (isBlock(x.toFloat(), y.toFloat(), z.toFloat())) {
      BLOCK
    } else {
      AIR
    }
  }

  private fun isBlock(x: Float, y: Float, z: Float): Boolean {
    val h = fbm_noise3(seed.toFloat() + x / dimension.width.toFloat(), seed.toFloat() + 0f, seed.toFloat() + z / dimension.depth.toFloat(), 2.0f, 0.5f, 6)
    return y < map(h, -0.5f..0.5f, (dimension.height.toFloat() * (6f/8f))..(dimension.height.toFloat() * (7f/8f)))
  }
}
