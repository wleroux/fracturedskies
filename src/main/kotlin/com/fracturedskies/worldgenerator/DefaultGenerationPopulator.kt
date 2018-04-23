package com.fracturedskies.worldgenerator

import com.fracturedskies.Block
import com.fracturedskies.api.BlockType
import com.fracturedskies.api.BlockType.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import java.lang.Integer.max


class DefaultGenerationPopulator(private val seed: Int): GenerationPopulator {
  override fun populate(blocks: ObjectMutableSpace<Block>, biomes: ObjectArea<Biome>) {
    (0 until blocks.dimension.width).forEach { x ->
      (0 until blocks.dimension.depth).forEach {  z ->
        (0 until blocks.dimension.height).forEach { y ->
          val blockPos = Vector3i(x, y, z)
          blocks[blockPos].type = if (isBlock(blocks.dimension, blockPos)) BlockType.STONE else BlockType.AIR
        }

        val pos = highestBlock(blocks, x, z)
        (max(0, pos.y - 6) until pos.y).forEach { newY ->
          if (blocks[pos.x, newY, pos.z].type != AIR)
            blocks[pos.x, newY, pos.z].type = DIRT
        }
        if (blocks[pos].type != AIR)
          blocks[pos].type = GRASS
      }
    }
  }

  private fun highestBlock(blocks: ObjectSpace<Block>, x: Int, z: Int) =
    Vector3i(x, (0 until blocks.dimension.height).reversed().firstOrNull { blocks[Vector3i(x, it, z)].type != AIR } ?: 0, z)

  private fun isBlock(dimension: Dimension, pos: Vector3i): Boolean {
    val h = fbm_noise3(seed.toFloat() + pos.x.toFloat() / dimension.width.toFloat(), seed.toFloat() + 0f, seed.toFloat() + pos.z.toFloat() / dimension.depth.toFloat(), 2.0f, 0.5f, 6)
    return pos.y.toFloat() < map(h, -0.5f..0.5f, (dimension.height.toFloat() * (6f/8f))..(dimension.height.toFloat() * (7f/8f)))
  }
}
