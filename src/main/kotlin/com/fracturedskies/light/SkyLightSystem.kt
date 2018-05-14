package com.fracturedskies.light

import com.fracturedskies.api.*
import com.fracturedskies.api.block.Block
import com.fracturedskies.api.block.data.SkyLight
import com.fracturedskies.engine.api.Cause
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import java.lang.Integer.max
import java.util.*
import javax.enterprise.event.Observes
import javax.inject.*
import kotlin.collections.HashSet

@Singleton
class SkyLightSystem {

  @Inject
  lateinit var world: World

  private var initialized = false
  private lateinit var light: IntMutableSpace
  private lateinit var opaque: BooleanMutableSpace

  fun onWorldGenerated(@Observes message: WorldGenerated) {
    message.blocks.forEach(this::updateLocalCache)

    val sky = mutableListOf<Vector3i>()
    (0 until world.width).forEach { x ->
      (0 until world.depth).forEach { z ->
        sky.add(Vector3i(x, world.height - 1, z))
      }
    }
    refresh(sky)
  }

  fun onBlocksUpdated(@Observes message: BlocksUpdated) {
    message.blocks.forEach { update -> updateLocalCache(update.position, update.target) }
    refresh(message.blocks
        .filter { it.original.type.opaque != it.target.type.opaque || it.original[SkyLight::class] != it.target[SkyLight::class] }
        .map(BlockUpdate::position))
  }

  private fun updateLocalCache(position: Vector3i, target: Block) {
    if (!initialized) {
      light = IntMutableSpace(world.dimension, {0})
      opaque = BooleanMutableSpace(world.dimension, {false})
      initialized = true
    }

    light[position] = target[SkyLight::class]!!.value
    opaque[position] = target.type.opaque
  }

  private fun refresh(positions: Collection<Vector3i>) {
    if (positions.isEmpty()) return

    val lightSources = mutableListOf<Vector3i>()

    // If not opaque, propagate the light!
    positions
        .filter { !opaque[it] }
        .toCollection(lightSources)

    // If opaque, any blocks that may have been lighted by this one are updated to zero...
    // Then any adjacent lights that may have lighted them are added to the list of light propagators
    val darkenedCells = darken(positions.filter { opaque[it] })

    darkenedCells.asSequence()
        .flatMap(light::neighbors)
        .toCollection(lightSources)
    darkenedCells
        .filter { it.y == light.height - 1 }
        .toCollection(lightSources)

    // Any position that is acting as a light source should propagate the light!
    val lightenedCells = lighten(lightSources.filter { !opaque[it] })

    // Update the world
    val blocks = (darkenedCells + lightenedCells)
        .filter { light[it] != world.blocks[it][SkyLight::class]!!.value }
        .distinct()
        .map { it to world.blocks[it].with(SkyLight(light[it])) }
        .toMap()
    if (blocks.isNotEmpty()) {
      world.updateBlocks(blocks, Cause.of(this))
    }
  }

  private fun darken(initialPositions: List<Vector3i>): Collection<Vector3i> {
    val darkenedCells = HashSet<Vector3i>()
    val unvisitedCells = LinkedList<Vector3i>()
    initialPositions.asSequence().flatMap(light::neighbors)
        .toCollection(unvisitedCells)
    while (unvisitedCells.isNotEmpty()) {
      val pos = unvisitedCells.remove()
      if (light[pos] == 0) continue

      light.neighbors(pos)
          .filter { !darkenedCells.contains(it) }
          .filter {
            if (pos.y > it.y) light[pos] >= light[it]
            else light[pos] > light[it]
          }
          .toCollection(unvisitedCells)

      darkenedCells.add(pos)
      light[pos] = 0
    }

    return darkenedCells
  }

  private fun targetLight(pos: Vector3i): Int {
    val topPos = light.top(pos)
    return when {
      topPos == null -> MAX_LIGHT_LEVEL
      light[topPos] == MAX_LIGHT_LEVEL -> MAX_LIGHT_LEVEL
      else -> max(0, (light.neighbors(pos).map {light[it]}.max() ?: 0) - 1)
    }
  }

  data class Lighten(val position: Vector3i, val targetLight: Int)
  private fun lighten(initialPositions: List<Vector3i>): Collection<Vector3i> {
    val lightenedCells = HashSet<Vector3i>()
    val unvisitedCells = PriorityQueue<Lighten>({ a, b -> -a.targetLight.compareTo(b.targetLight)})
    initialPositions
        .asSequence()
        .map { Lighten(it, targetLight(it)) }
        .toCollection(unvisitedCells)
    while (unvisitedCells.isNotEmpty()) {
      val (pos, targetLight) = unvisitedCells.poll()
      if (light[pos] >= targetLight) continue

      light[pos] = targetLight
      lightenedCells.add(pos)

      world.neighbors(pos)
          .filter { !opaque[it] }
          .map {
            val propagateLight = when {
              targetLight == MAX_LIGHT_LEVEL && pos.y > it.y -> MAX_LIGHT_LEVEL
              else -> targetLight - 1
            }
            Lighten(it, propagateLight)
          }
          .filter { light[it.position] < it.targetLight }
          .filter { it.targetLight > 0 }
          .toCollection(unvisitedCells)
    }

    return lightenedCells
  }
}