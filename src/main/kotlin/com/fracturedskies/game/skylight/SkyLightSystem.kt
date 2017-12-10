package com.fracturedskies.game.skylight

import com.fracturedskies.UI_CONTEXT
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.game.messages.SkyLightUpdated
import com.fracturedskies.game.messages.UpdateBlock
import com.fracturedskies.game.messages.WorldGenerated
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

class SkyLightSystem(coroutineContext: CoroutineContext) {
  companion object {
    val MAX_LIGHT_LEVEL = 15
  }

  lateinit var light: LightMap
  val channel = MessageChannel(coroutineContext + UI_CONTEXT) { message ->
    when (message) {
      is WorldGenerated -> {
        val world = message.world
        light = LightMap(world.dimension)
        (0 until light.dimension.width).forEach { x ->
          (0 until light.dimension.depth).forEach { z ->
            (0 until light.dimension.height).forEach { y ->
              light.type[x, y, z] = world[x, y, z].type
            }
          }
        }

        // All surface opaque have max skylight level
        val skyLightUpdates = mutableMapOf<Vector3i, Int>()
        val skyLightPropagation = LinkedList<Vector3i>()
        (0 until light.dimension.width).forEach { x ->
          (0 until light.dimension.depth).forEach { z ->
            val highestOpaqueBlock = (0 until light.dimension.height).reversed().firstOrNull {
              light.type[x, it, z].opaque
            } ?: 0

            ((highestOpaqueBlock + 1) until light.dimension.height).forEach { y ->
              val lightPos = Vector3i(x, y, z)
              skyLightUpdates.put(lightPos, MAX_LIGHT_LEVEL)
              skyLightPropagation.add(lightPos)
              light.level[x, y, z] = MAX_LIGHT_LEVEL
            }
          }
        }
        skyLightUpdates.putAll(propagateSkyLight(skyLightPropagation))
        send(SkyLightUpdated(skyLightUpdates, Cause.of(message.cause, this), message.context))
      }
      is UpdateBlock -> {
        val lightUpdates = message.updates.map { (pos, type) ->
          light.type[pos] = type
          updateSkylight(pos)
        }.fold(mutableMapOf<Vector3i, Int>()) { acc, value ->
          acc.putAll(value)
          acc
        }

        if (lightUpdates.isNotEmpty()) {
          send(SkyLightUpdated(lightUpdates, Cause.of(message.cause, this), message.context))
        }
      }
    }
  }

  private fun propagateSkyLight(lightPropagation: LinkedList<Vector3i>): Map<Vector3i, Int> {
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
            lightUpdates.put(neighborPos, propagateLightValue)
            light.level[neighborPos] = propagateLightValue
            lightPropagation.addLast(neighborPos)
          }
        }
      }
    }
    return lightUpdates
  }

  private fun updateSkylight(initialPos: Vector3i): Map<Vector3i, Int> {
    val lightUpdates = mutableMapOf<Vector3i, Int>()
    if (light.type[initialPos].opaque) {
      // Remove Skylight
      if (light.level[initialPos] == 0)
        return lightUpdates

      lightUpdates.put(initialPos, 0)
      light.level[initialPos] = 0
      val lightPos = LinkedList<Vector3i>()
      val skyunlightPos = LinkedList<Vector3i>(Vector3i.NEIGHBOURS
              .map { initialPos + it }
              .filter { light.has(it) })
      while (skyunlightPos.isNotEmpty()) {
        val blockPos = skyunlightPos.pollFirst()
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
          lightUpdates.put(blockPos, 0)
          light.level[blockPos] = 0
          Vector3i.NEIGHBOURS.forEach { neighborVector ->
            val neighborPos = blockPos + neighborVector
            if (light.has(neighborPos)) {
              skyunlightPos.addLast(neighborPos)
            }
          }
        }
      }

      lightUpdates.putAll(propagateSkyLight(lightPos))
      return lightUpdates
    } else {
      val skyLightLevel = if (initialPos.y + 1 == light.dimension.height) MAX_LIGHT_LEVEL else 0
      lightUpdates.put(initialPos, skyLightLevel)
      light.level[initialPos] = skyLightLevel
      val skylightPos = LinkedList(Vector3i.NEIGHBOURS
              .map { initialPos + it }
              .filter { light.has(it) })
      skylightPos.addFirst(initialPos)
      lightUpdates.putAll(propagateSkyLight(skylightPos))
      return lightUpdates
    }
  }
}