package com.fracturedskies.water

import com.fracturedskies.api.*
import com.fracturedskies.api.block.data.WaterLevel
import com.fracturedskies.engine.api.*
import com.fracturedskies.engine.collections.forEach
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_Y
import com.fracturedskies.engine.math.Vector3i.Companion.xZSpiral
import com.fracturedskies.water.WaterMap.Companion.MAX_WATER_RANGE
import java.lang.Integer.min
import java.util.*
import java.util.Comparator.comparingInt
import java.util.function.ToIntFunction
import javax.enterprise.event.Observes
import javax.inject.*

@Singleton
class WaterSystem {

  @Inject
  lateinit var world: World

  private var initialized = false
  private lateinit var water: WaterMap
  private lateinit var pathFinder: PathFinder

  fun onNewGameRequested(@Observes newGameRequested: NewGameRequested) {
    if (!initialized) {
      water = WaterMap(world.dimension)
      pathFinder = PathFinder({ _, toPos -> water.has(toPos) && water.maxFlowOut[toPos] != 0.toByte() })
      initialized = true
    }
  }

  fun onWorldGenerated(@Observes worldGenerated: WorldGenerated) {
    if (!initialized) {
      water = WaterMap(world.dimension)
      pathFinder = PathFinder({ _, toPos -> water.has(toPos) && water.maxFlowOut[toPos] != 0.toByte() })
      initialized = true
    }

    val waterLevelUpdates = mutableMapOf<Vector3i, Byte>()
    worldGenerated.blocks.forEach { pos, block ->
      water.setOpaque(pos, block.type.opaque)
      if (block.type.opaque && water.getLevel(pos) != 0.toByte()) {
        water.setLevel(pos, 0)
        waterLevelUpdates[pos] = 0
      }
    }
    worldGenerated.blocks.forEach { pos, _ ->
      water.nearestWaterDrop[pos] = calculateNearestWaterDrop(pos)
    }
    if (waterLevelUpdates.isNotEmpty()) {
      world.updateBlocks(waterLevelUpdates
          .map { it.key to world.blocks[it.key].with(WaterLevel(it.value)) }
          .toMap(), Cause.of(this))
    }
  }

  fun onBlockUpdated(@Observes blocksUpdated: BlocksUpdated) {
    if (!initialized) {
      water = WaterMap(world.dimension)
      pathFinder = PathFinder({ _, toPos -> water.has(toPos) && water.maxFlowOut[toPos] != 0.toByte() })
      initialized = true
    }

    val waterLevelUpdates = mutableMapOf<Vector3i, Byte>()
    blocksUpdated.blocks.forEach { (pos, block) ->
      water.setOpaque(pos, block.type.opaque)
      water.setLevel(pos, block[WaterLevel::class]!!.value)
      if (block.type.opaque && water.getLevel(pos) != 0.toByte()) {
        water.setLevel(pos, 0)
        waterLevelUpdates[pos] = 0
      }
    }
    blocksUpdated.blocks.forEach { (pos, _) ->
      updateNearestWater(pos)
    }
    if (waterLevelUpdates.isNotEmpty()) {
      world.updateBlocks(waterLevelUpdates
          .map { it.key to world.blocks[it.key].with(WaterLevel(it.value)) }
          .toMap(), Cause.of(this))
    }
  }

  fun onUpdate(@Observes update: Update) {
    val flow = flow()
    val evaporation = evaporation()

    val updates = flow + evaporation
    if (updates.isNotEmpty()) {
      world.updateBlocks(updates
          .map { it.key to world.blocks[it.key].with(WaterLevel(it.value)) }
          .toMap(), Cause.of(this))
    }
  }

  private fun updateNearestWater(pos: Vector3i) {
    xZSpiral(MAX_WATER_RANGE)
        .flatMap { listOf(pos + AXIS_Y + it, pos + it) }
        .filter { water.has(it) }
        .forEach {
          val newValue = calculateNearestWaterDrop(it)
          if (water.nearestWaterDrop[it] != newValue) {
            water.nearestWaterDrop[it] = newValue
            water.sea[it]?.disturbed = true
          }
        }
  }

  private fun calculateNearestWaterDrop(pos: Vector3i): Int {
    if (water.getOpaque(pos)) return WaterMap.MAX_WATER_RANGE
    return xZSpiral(MAX_WATER_RANGE).map { pos - AXIS_Y + it }
        .filter { water.has(it) }
        .filter { ! water.getOpaque(it) }
        .map { min(it distanceTo (pos - Vector3i.AXIS_Y), MAX_WATER_RANGE) }
        .min() ?: MAX_WATER_RANGE
  }

  private fun evaporation(): Map<Vector3i, Byte> {
    val evaporation = water.evaporationCandidates
            .filter { Math.random() > 0.99995 }
    return if (evaporation.isNotEmpty()) {
      val waterLevelUpdates = mutableMapOf<Vector3i, Byte>()
      for (pos in evaporation) {
        water.setLevel(pos, 0)
        waterLevelUpdates[pos] = 0
      }
      waterLevelUpdates
    } else {
      mapOf()
    }
  }

  private fun flow(): Map<Vector3i, Byte> {
    water.maxFlowOut.clear()
    val originalWaterLevel = mutableMapOf<Vector3i, Byte>()
    val waterLevelUpdates = mutableMapOf<Vector3i, Byte>()
    val disturbedSeas = water.seas.filter { it.disturbed }
    disturbedSeas.forEach { it.disturbed = false }
    val providences = disturbedSeas.map {it.providence.toList()}
    for (providence in providences) {
      // Get all potential flow candidates
      for (adjacent in providence)
        water.maxFlowOut[adjacent] = water.getLevel(adjacent)

      Collections.shuffle(providence)
      val candidates = PriorityQueue<Vector3i>(waterPotentialComparator)
      candidates.addAll(providence)

      // Process water
      while (!candidates.isEmpty()) {
        // Find path from water source to water target
        val targetLocation = candidates.poll()
        val targetWaterLevel = water.getLevel(targetLocation)
        if (targetWaterLevel >= MAX_WATER_LEVEL) {
          continue
        }

        val targetWaterPotential = waterPotential(targetLocation) + 2
        val results = pathFinder.find(targetLocation, { waterPotential(it) >= targetWaterPotential}, { _, it -> -waterPotential(it)}, true)
        val path = results.path
        if (!path.isEmpty()) {
          path.stream().skip(1).forEach { water.maxFlowOut[it]-- }

          val sourceLocation = path.last()
          val sourceWaterLevel = water.getLevel(sourceLocation)

          originalWaterLevel.putIfAbsent(sourceLocation, sourceWaterLevel)
          originalWaterLevel.putIfAbsent(targetLocation, targetWaterLevel)
          val newSourceWaterLevel = sourceWaterLevel.dec()
          val newTargetWaterLevel = targetWaterLevel.inc()
          water.setLevel(sourceLocation, newSourceWaterLevel)
          water.setLevel(targetLocation, newTargetWaterLevel)
          waterLevelUpdates[sourceLocation] = newSourceWaterLevel
          waterLevelUpdates[targetLocation] = newTargetWaterLevel

          candidates.remove(sourceLocation)
          candidates.add(sourceLocation)
          if (targetWaterLevel != MAX_WATER_LEVEL)
            candidates.add(targetLocation)
        } else {
          for (entry in results.cameFrom) {
            val to = entry.key
            var from = entry.value
            if (from != null) {
              val dir = when {
                to.x > from.x -> Vector3i.AXIS_X
                to.x < from.x -> Vector3i.AXIS_NEG_X
                to.y > from.y -> AXIS_Y
                to.y < from.y -> Vector3i.AXIS_NEG_Y
                to.z > from.z -> Vector3i.AXIS_Z
                else -> Vector3i.AXIS_NEG_Z
              }

              while (from != to) {
                water.maxFlowOut[from] = 0
                from += dir
              }
            }
            water.maxFlowOut[to] = 0
          }
        }
      }
    }

    // Split seas that have settled
    disturbedSeas.filterNot { it.disturbed }.forEach { water.splitSea(it) }

    // Only update water levels for actual change (rather than pass-through changes)
    return waterLevelUpdates.filter { (pos, waterLevel) -> originalWaterLevel[pos] != waterLevel }
  }

  private fun waterPotential(pos: Vector3i) = pos.y * (MAX_WATER_LEVEL + MAX_WATER_RANGE + 1) + water.nearestWaterDrop[pos] + water.getLevel(pos)

  private val waterPotentialComparator = comparingInt(ToIntFunction<Vector3i> { waterPotential(it) })
}
