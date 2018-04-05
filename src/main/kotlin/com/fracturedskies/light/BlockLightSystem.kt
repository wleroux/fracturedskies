package com.fracturedskies.light

import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.engine.messages.MessageChannel
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

fun blockLightSystem(context: CoroutineContext): MessageChannel {
  class BlockLightMap(val dimension: Dimension) {
    val level = IntMutableSpace(dimension)
    val type = ObjectMutableSpace(dimension, { AIR })

    fun has(pos: Vector3i) = has(pos.x, pos.y, pos.z)
    fun has(x: Int, y: Int, z: Int) = dimension.has(x, y, z)
  }
  lateinit var light: BlockLightMap

  fun propagateBlockLight(lightPropagation: LinkedList<Vector3i>): Map<Vector3i, Int> {
    val lightUpdates = mutableMapOf<Vector3i, Int>()
    while (lightPropagation.isNotEmpty()) {
      val blockPos = lightPropagation.pollFirst()
      if (light.level[blockPos] == 0)
        continue

      Vector3i.NEIGHBOURS
          .filter { light.has(blockPos + it) }
          .forEach { neighborVector ->
            val neighborPos = blockPos + neighborVector
            val propagateLightValue = light.level[blockPos] - 1
            if (!light.type[neighborPos].opaque && light.level[neighborPos] < propagateLightValue) {
              lightUpdates.put(neighborPos, propagateLightValue)
              light.level[neighborPos] = propagateLightValue
              lightPropagation.addLast(neighborPos)
            }
          }
    }
    return lightUpdates
  }

  fun updateBlockLight(initialPos: Vector3i): Map<Vector3i, Int> {
    val lightUpdates = mutableMapOf<Vector3i, Int>()
    if (light.type[initialPos].blockLight == 0) {
      // Remove BlockLight
      if (light.level[initialPos] != 0) {
        lightUpdates[initialPos] = 0
        light.level[initialPos] = 0
      }

      val lightPos = LinkedList<Vector3i>()
      val adjacentBlockPos = LinkedList<Vector3i>(Vector3i.NEIGHBOURS
          .map { initialPos + it }
          .filter { light.has(it) })
      while (adjacentBlockPos.isNotEmpty()) {
        val blockPos = adjacentBlockPos.pollFirst()
        if (light.level[blockPos] == 0)
          continue
        val lit = (light.type[blockPos].opaque && light.level[blockPos] > 0) || Vector3i.NEIGHBOURS
            .filter { light.has(blockPos + it) }
            .any { (light.level[blockPos] < light.level[blockPos + it]) }
        if (lit) {
          lightPos.add(blockPos)
        } else if (!light.type[blockPos].opaque) {
          lightUpdates[blockPos] = 0
          light.level[blockPos] = 0
          Vector3i.NEIGHBOURS
              .filter { light.has(blockPos + it) }
              .forEach { adjacentBlockPos.addLast(blockPos + it) }
        }
      }

      lightUpdates.putAll(propagateBlockLight(lightPos))
      return lightUpdates
    } else {
      val blockLightLevel = light.type[initialPos].blockLight
      lightUpdates[initialPos] = blockLightLevel
      light.level[initialPos] = blockLightLevel
      val blockLightPos = LinkedList(Vector3i.NEIGHBOURS
          .map { initialPos + it }
          .filter { light.has(it) })
      blockLightPos.addFirst(initialPos)
      lightUpdates.putAll(propagateBlockLight(blockLightPos))
      return lightUpdates
    }
  }

  return MessageChannel(context) { message ->
    when (message) {
      is NewGameRequested -> {
        light = BlockLightMap(message.dimension)
      }
      is BlockUpdated -> {
        val blockLightUpdates = message.updates.map { (pos, type) ->
          light.type[pos] = type
          updateBlockLight(pos)
        }.fold(mutableMapOf<Vector3i, Int>()) { acc, value ->
          acc.putAll(value)
          acc
        }

        if (blockLightUpdates.isNotEmpty()) {
          send(BlockLightUpdated(blockLightUpdates, message.cause, message.context))
        }
      }
    }
  }
}