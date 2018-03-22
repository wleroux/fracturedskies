package com.fracturedskies.light

import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.light.api.MAX_LIGHT_LEVEL
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

fun skyLightSystem(context: CoroutineContext): MessageChannel {
  class SkyLightMap(val dimension: Dimension) {
    val level = IntMap(dimension)
    val type = ObjectMap(dimension, { AIR })

    fun has(pos: Vector3i) = has(pos.x, pos.y, pos.z)
    fun has(x: Int, y: Int, z: Int) = dimension.has(x, y, z)
  }
  lateinit var light: SkyLightMap

  fun propagateSkyLight(lightPropagation: LinkedList<Vector3i>): Map<Vector3i, Int> {
    val lightUpdates = mutableMapOf<Vector3i, Int>()
    while (lightPropagation.isNotEmpty()) {
      val blockPos = lightPropagation.pollFirst()
      if (light.level[blockPos] == 0)
        continue

      Vector3i.NEIGHBOURS.forEach { neighborVector ->
        val neighborPos = blockPos + neighborVector
        if (light.has(neighborPos)) {
          val propagateLightValue = if (light.level[blockPos] == MAX_LIGHT_LEVEL && neighborVector == Vector3i.AXIS_NEG_Y) {
            MAX_LIGHT_LEVEL
          } else if (neighborPos.y + 1 == light.dimension.height) {
            MAX_LIGHT_LEVEL
          } else {
            light.level[blockPos] - 1
          }

          if (!light.type[neighborPos].opaque && light.level[neighborPos] < propagateLightValue) {
            lightUpdates[neighborPos] = propagateLightValue
            light.level[neighborPos] = propagateLightValue
            lightPropagation.addLast(neighborPos)
          }
        }
      }
    }
    return lightUpdates
  }

  fun updateSkylight(initialPos: Vector3i): Map<Vector3i, Int> {
    val lightUpdates = mutableMapOf<Vector3i, Int>()
    if (light.type[initialPos].opaque) {
      // Remove Skylight
      if (light.level[initialPos] == 0)
        return lightUpdates

      lightUpdates[initialPos] = 0
      light.level[initialPos] = 0
      val lightPos = LinkedList<Vector3i>()
      val skyunlitPos = LinkedList<Vector3i>(Vector3i.NEIGHBOURS
          .map { initialPos + it }
          .filter { light.has(it) })
      while (skyunlitPos.isNotEmpty()) {
        val blockPos = skyunlitPos.pollFirst()
        if (light.level[blockPos] == 0 || light.type[blockPos].opaque)
          continue
        val lit = (blockPos.y == light.dimension.height - 1) || Vector3i.NEIGHBOURS
            .filter { light.has(blockPos + it) }
            .any {
              if (it == Vector3i.AXIS_Y &&
                  light.level[blockPos + it] == MAX_LIGHT_LEVEL &&
                  light.level[blockPos] <= light.level[blockPos + it]) {
                true
              } else {
                (light.level[blockPos] < light.level[blockPos + it])
              }
            }
        if (lit) {
          lightPos.add(blockPos)
        } else {
          lightUpdates[blockPos] = 0
          light.level[blockPos] = 0
          Vector3i.NEIGHBOURS.forEach { neighborVector ->
            val neighborPos = blockPos + neighborVector
            if (light.has(neighborPos)) {
              skyunlitPos.addLast(neighborPos)
            }
          }
        }
      }

      lightUpdates.putAll(propagateSkyLight(lightPos))
      return lightUpdates
    } else {
      val skyLightLevel = if (initialPos.y + 1 == light.dimension.height) MAX_LIGHT_LEVEL else 0
      lightUpdates[initialPos] = skyLightLevel
      light.level[initialPos] = skyLightLevel
      val skylightPos = LinkedList(Vector3i.NEIGHBOURS
          .map { initialPos + it }
          .filter { light.has(it) })
      skylightPos.addFirst(initialPos)
      lightUpdates.putAll(propagateSkyLight(skylightPos))
      return lightUpdates
    }
  }

  return MessageChannel(context) { message ->
    when (message) {
      is NewGameRequested -> {
        light = SkyLightMap(message.dimension)
      }
      is UpdateBlock -> {
        message.updates.forEach { pos, type ->
          light.type[pos] = type
        }

        val lightUpdates = message.updates.flatMap { (pos, _) ->
          updateSkylight(pos).toList()
        }.toMap()

        if (lightUpdates.isNotEmpty()) {
          send(SkyLightUpdated(lightUpdates, message.cause, message.context))
        }
      }
    }
  }
}