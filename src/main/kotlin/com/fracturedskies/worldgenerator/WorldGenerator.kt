package com.fracturedskies.worldgenerator

import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send


class WorldGenerator(private val dimension: Dimension, private val seed: Int) {
  fun generate() {
    val blocks = ObjectMutableSpace(dimension, {BlockType.AIR})
    (0 until dimension.width).forEach { x ->
      (0 until dimension.depth).forEach {  z ->
        (0 until dimension.height).forEach { y ->
          val blockPos = Vector3i(x, y, z)
          blocks[blockPos] = if (isBlock(blockPos)) BlockType.BLOCK else BlockType.AIR
        }

        val pos = highestBlock(blocks, x, z)
        if (blocks[pos] != AIR)
          blocks[pos] = GRASS
      }
    }

    send(BlocksGenerated(Vector3i(0, 0, 0), blocks, Cause.of(this)))
  }

  private fun highestBlock(blockType: ObjectSpace<BlockType>, x: Int, z: Int) =
    Vector3i(x, (0 until dimension.height).reversed().firstOrNull { blockType[Vector3i(x, it, z)] != AIR } ?: 0, z)

  private fun isBlock(pos: Vector3i): Boolean {
    val h = fbm_noise3(seed.toFloat() + pos.x.toFloat() / dimension.width.toFloat(), seed.toFloat() + 0f, seed.toFloat() + pos.z.toFloat() / dimension.depth.toFloat(), 2.0f, 0.5f, 6)
    return pos.y.toFloat() < map(h, -0.5f..0.5f, (dimension.height.toFloat() * (6f/8f))..(dimension.height.toFloat() * (7f/8f)))
  }
}
