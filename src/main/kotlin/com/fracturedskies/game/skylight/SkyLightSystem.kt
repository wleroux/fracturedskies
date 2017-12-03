package com.fracturedskies.game.skylight

import com.fracturedskies.UI_CONTEXT
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.game.messages.LightUpdated
import com.fracturedskies.game.messages.UpdateBlock
import com.fracturedskies.game.messages.WorldGenerated
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

class SkyLightSystem(coroutineContext: CoroutineContext) {
  companion object {
    val MAX_SKYLIGHT_LEVEL = 16
  }

  lateinit var skylight: SkyLightMap
  val channel = MessageChannel(coroutineContext + UI_CONTEXT) { message ->
    when (message) {
      is WorldGenerated -> {
        val world = message.world
        skylight = SkyLightMap(world.width, world.height, world.depth)
        (0 until skylight.width).forEach { x ->
          (0 until skylight.depth).forEach { z ->
            (0 until skylight.height).forEach { y ->
              skylight.opaque[x, y, z] = world[x, y, z].type.opaque
            }
          }
        }

        // All surface opaque have max skylight level
        val lightUpdates = mutableMapOf<Vector3i, Int>()
        val lightPropagation = LinkedList<Vector3i>()
        (0 until skylight.width).forEach { x ->
          (0 until skylight.depth).forEach { z ->
            val highestOpaqueBlock = (0 until skylight.height).reversed().firstOrNull {
              skylight.opaque[x, it, z]
            } ?: 0

            ((highestOpaqueBlock + 1) until skylight.height).forEach { y ->
              val lightPos = Vector3i(x, y, z)
              lightUpdates.put(lightPos, MAX_SKYLIGHT_LEVEL)
              lightPropagation.add(lightPos)
              skylight.level[x, y, z] = MAX_SKYLIGHT_LEVEL
            }
          }
        }
        lightUpdates.putAll(propagateLight(lightPropagation))
        send(LightUpdated(lightUpdates, Cause.of(message.cause, this), message.context))
      }
      is UpdateBlock -> {
        skylight.opaque[message.pos] = message.type.opaque
        val lightUpdates = updateSkylight(message.pos)
        if (lightUpdates.isNotEmpty()) {
          send(LightUpdated(lightUpdates, Cause.of(message.cause, this), message.context))
        }
      }
    }
  }

  private fun propagateLight(lightPropagation: LinkedList<Vector3i>): Map<Vector3i, Int> {
    val lightUpdates = mutableMapOf<Vector3i, Int>()
    while (lightPropagation.isNotEmpty()) {
      val blockPos = lightPropagation.pollFirst()
      if (skylight.level[blockPos] == 0)
        continue

      Vector3i.NEIGHBOURS.forEach { neighborVector ->
        val neighborPos = blockPos + neighborVector
        if (skylight.has(neighborPos)) {
          val propagateLightValue = if (skylight.level[blockPos] == MAX_SKYLIGHT_LEVEL && neighborVector == Vector3i.AXIS_NEG_Y) {
            MAX_SKYLIGHT_LEVEL
          } else if (neighborPos.y + 1 == skylight.height) {
            MAX_SKYLIGHT_LEVEL
          } else {
            skylight.level[blockPos] - 1
          }

          if (!skylight.opaque[neighborPos] && skylight.level[neighborPos] < propagateLightValue) {
            lightUpdates.put(neighborPos, propagateLightValue)
            skylight.level[neighborPos] = propagateLightValue
            lightPropagation.addLast(neighborPos)
          }
        }
      }
    }
    return lightUpdates
  }

  private fun updateSkylight(initialPos: Vector3i): Map<Vector3i, Int> {
    val lightUpdates = mutableMapOf<Vector3i, Int>()
    if (skylight.opaque[initialPos]) {
      // Remove Skylight
      if (skylight.level[initialPos] == 0)
        return lightUpdates

      lightUpdates.put(initialPos, 0)
      skylight.level[initialPos] = 0
      val skylightPos = LinkedList<Vector3i>()
      val skyunlightPos = LinkedList<Vector3i>(Vector3i.NEIGHBOURS
              .map { initialPos + it }
              .filter { skylight.has(it) })
      while (skyunlightPos.isNotEmpty()) {
        val blockPos = skyunlightPos.pollFirst()
        if (skylight.level[blockPos] == 0 || skylight.opaque[blockPos])
          continue
        val lit = (blockPos.y == skylight.height - 1) || Vector3i.NEIGHBOURS
                .filter { skylight.has(blockPos + it) }
                .any {
                  if (it == Vector3i.AXIS_Y &&
                          skylight.level[blockPos + it] == MAX_SKYLIGHT_LEVEL &&
                          skylight.level[blockPos] <= skylight.level[blockPos + it]) {
                    true
                  } else {
                    (skylight.level[blockPos] < skylight.level[blockPos + it])
                  }
                }
        if (lit) {
          skylightPos.add(blockPos)
        } else {
          lightUpdates.put(blockPos, 0)
          skylight.level[blockPos] = 0
          Vector3i.NEIGHBOURS.forEach { neighborVector ->
            val neighborPos = blockPos + neighborVector
            if (skylight.has(neighborPos)) {
              skyunlightPos.addLast(neighborPos)
            }
          }
        }
      }

      lightUpdates.putAll(propagateLight(skylightPos))
      return lightUpdates
    } else {
      val skyLightLevel = if (initialPos.y + 1 == skylight.height) MAX_SKYLIGHT_LEVEL else 0
      lightUpdates.put(initialPos, skyLightLevel)
      skylight.level[initialPos] = skyLightLevel
      val skylightPos = LinkedList(Vector3i.NEIGHBOURS
              .map { initialPos + it }
              .filter { skylight.has(it) })
      skylightPos.addFirst(initialPos)
      lightUpdates.putAll(propagateLight(skylightPos))
      return lightUpdates
    }
  }
}