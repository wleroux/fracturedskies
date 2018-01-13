package com.fracturedskies.game.skylight

import com.fracturedskies.UI_CONTEXT
import com.fracturedskies.engine.collections.Dimension
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.game.messages.*
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

class BlockLightSystem(coroutineContext: CoroutineContext, dimension: Dimension) {
  private val light = LightMap(dimension)
  val channel = MessageChannel(coroutineContext + UI_CONTEXT) { message ->
    when (message) {
      is UpdateBlock -> {
        val blockLightUpdates = message.updates.map { (pos, type) ->
          light.type[pos] = type
          updateBlockLight(pos)
        }.fold(mutableMapOf<Vector3i, Int>()) { acc, value ->
          acc.putAll(value)
          acc
        }

        if (blockLightUpdates.isNotEmpty()) {
          send(BlockLightUpdated(blockLightUpdates, Cause.of(message.cause, this), message.context))
        }
      }
    }
  }

  private fun propagateBlockLight(lightPropagation: LinkedList<Vector3i>): Map<Vector3i, Int> {
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

  private fun updateBlockLight(initialPos: Vector3i): Map<Vector3i, Int> {
    val lightUpdates = mutableMapOf<Vector3i, Int>()
    if (light.type[initialPos].blockLight == 0) {
      // Remove BlockLight
      if (light.level[initialPos] != 0) {
        lightUpdates.put(initialPos, 0)
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
          lightUpdates.put(blockPos, 0)
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
      lightUpdates.put(initialPos, blockLightLevel)
      light.level[initialPos] = blockLightLevel
      val blockLightPos = LinkedList(Vector3i.NEIGHBOURS
              .map { initialPos + it }
              .filter { light.has(it) })
      blockLightPos.addFirst(initialPos)
      lightUpdates.putAll(propagateBlockLight(blockLightPos))
      return lightUpdates
    }
  }
}