package com.fracturedskies.light

import com.fracturedskies.api.*
import com.fracturedskies.api.block.Block
import com.fracturedskies.api.block.data.BlockLight
import com.fracturedskies.engine.api.Cause
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import java.lang.Integer.max
import java.util.*
import javax.enterprise.event.Observes
import javax.inject.*

@Singleton
class BlockLightSystem {

  @Inject
  lateinit var world: World

  private var initialized = false
  private lateinit var light: IntMutableSpace
  private lateinit var opaque: BooleanMutableSpace

  fun onWorldGenerated(@Observes message: WorldGenerated) {
    message.blocks.forEach(this::updateLocalCache)
  }

  fun onBlocksUpdated(@Observes message: BlocksUpdated) {
    message.blocks.forEach(this::updateLocalCache)
    refresh(message.blocks.keys)
  }

  private fun updateLocalCache(update: Map.Entry<Vector3i, Block>) {
    if (!initialized) {
      light = IntMutableSpace(world.dimension, {0})
      opaque = BooleanMutableSpace(world.dimension, {false})
      initialized = true
    }

    light[update.key] = update.value[BlockLight::class]!!.value
    opaque[update.key] = update.value.type.opaque
  }

  private fun refresh(positions: Collection<Vector3i>) {
    val lightSources = mutableListOf<Vector3i>()

    // If not opaque, propagate the light!
    positions.toCollection(lightSources)

    // If opaque, any blocks that may have been lighted by this one are updated to zero...
    // Then any adjacent lights that may have lighted them are added to the list of light propagators
    val darkenedCells = darken(positions.filter { opaque[it] })

    darkenedCells
        .flatMap(light::neighbors)
        .toCollection(lightSources)
    darkenedCells
        .toCollection(lightSources)

    // Any position that is acting as a light source should propagate the light!
    val lightenedCells = lighten(lightSources)

    // Update the world
    val blocks = (darkenedCells + lightenedCells)
        .filter { light[it] != world.blocks[it][BlockLight::class]!!.value }
        .distinct()
        .map { it to world.blocks[it].with(BlockLight(light[it])) }
        .toMap()
    if (blocks.isNotEmpty()) {
      world.updateBlocks(blocks, Cause.of(this))
    }
  }

  private fun darken(initialPositions: List<Vector3i>): List<Vector3i> {
    val darkenedCells = LinkedList<Vector3i>()
    val unvisitedCells = LinkedList<Vector3i>()
    initialPositions.flatMap(light::neighbors)
        .toCollection(unvisitedCells)
    while (unvisitedCells.isNotEmpty()) {
      val pos = unvisitedCells.remove()
      if (light[pos] == 0) continue

      light.neighbors(pos)
          .filter { light[pos] > light[it] }
          .toCollection(unvisitedCells)

      darkenedCells.add(pos)
      light[pos] = 0
    }

    return darkenedCells
  }

  private fun lighten(initialPositions: List<Vector3i>): List<Vector3i> {
    val lightenedCells = LinkedList<Vector3i>()
    val unvisitedCells = LinkedList<Vector3i>()
    initialPositions.toCollection(unvisitedCells)
    while (unvisitedCells.isNotEmpty()) {
      val pos = unvisitedCells.remove()
      val targetLight = max(
          world.blocks[pos].type.light,
          (light.neighbors(pos).map {light[it]}.max() ?: 0) - 1
      )
      if (light[pos] >= targetLight) continue

      light[pos] = targetLight
      world.neighbors(pos)
          .filter { !opaque[it] }
          .filter { light[pos] > light[it] }
          .toCollection(unvisitedCells)

      lightenedCells.add(pos)
    }

    return lightenedCells
  }
}