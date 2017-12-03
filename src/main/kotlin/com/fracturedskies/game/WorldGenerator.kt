package com.fracturedskies.game

import com.fracturedskies.engine.math.fbm_noise3
import com.fracturedskies.engine.math.map


class WorldGenerator(private val width: Int, private val height: Int, private val depth: Int, private val seed: Int): (Int, Int, Int) -> Block {
  fun generate(): World {
    val world = World(width, height, depth, this)
    (0 until width).forEach {x ->
      (0 until depth).forEach {z ->
        val highestBlock = highestBlock(world, x, z)
        world[x, highestBlock, z].type = BlockType.GRASS
      }
    }

    return world
  }

  private fun highestBlock(world: World, x: Int, z: Int) =
    (0 until world.height).reversed().firstOrNull { world[x, it, z].type != BlockType.AIR } ?: 0

  override operator fun invoke(x: Int, y: Int, z: Int): Block {
    return if (isBlock(x.toFloat(), y.toFloat(), z.toFloat())) {
      Block(BlockType.BLOCK, 0, 0)
    } else {
      Block(BlockType.AIR, 0, 0)
    }
  }

  private fun isBlock(x: Float, y: Float, z: Float): Boolean {
    seed.hashCode()
    val h = fbm_noise3(seed.toFloat() + x / width.toFloat(), seed.toFloat() + 0f, seed.toFloat() + z / depth.toFloat(), 2.0f, 0.5f, 6)
    return y < map(h, -0.5f..0.5f, (height.toFloat() * (6f/8f))..(height.toFloat() * (7f/8f)))
  }
}
