package com.fracturedskies.game.water

import com.fracturedskies.engine.Update
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.game.messages.UpdateBlock
import com.fracturedskies.game.messages.UpdateBlockWater
import com.fracturedskies.game.messages.WorldGenerated
import java.util.*
import java.util.function.ToIntFunction
import kotlin.coroutines.experimental.CoroutineContext

class WaterSystem(coroutineContext: CoroutineContext) {
  companion object {
    val MAX_WATER_LEVEL = 3.toByte()
  }

  private lateinit var pathFinder: WaterPathFinder
  private var started = false
  private lateinit var water: WaterMap
  val channel = MessageChannel(coroutineContext) { message ->
    when (message) {
      is WorldGenerated -> {
        val world = message.world
        water = WaterMap(world.dimension)
        pathFinder = WaterPathFinder(water)
        (0 until water.dimension.height).forEach { y ->
          (0 until water.dimension.width).forEach { x->
            (0 until water.dimension.depth).forEach {z ->
              water.setOpaque(Vector3i(x, y, z), world[x, y, z].type.opaque)
            }
          }
        }
        started = true
      }
      is UpdateBlock -> {
        if (started) {
          message.updates.forEach { pos, type ->
            water.setOpaque(pos, type.opaque)
          }
        }
      }
      is UpdateBlockWater -> {
        if (message.cause.first() != this) {
          message.updates.forEach { (pos, waterLevel) ->
            water.setLevel(pos, waterLevel)
          }
        }
      }
      is Update -> {
        if (started) {
          val flow = flow()
          val evaporation = evaporation()

          val updates = flow + evaporation
          if (updates.isNotEmpty()) {
            MessageBus.send(UpdateBlockWater(updates, Cause.of(this), Context()))
          }
        }
      }
    }
  }

  private fun evaporation(): Map<Vector3i, Byte> {
    val evaporation = water.evaporationCandidates
            .filter { Math.random() > 0.995 }
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

  private suspend fun flow(): Map<Vector3i, Byte> {
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

        val path = pathFinder.find(targetLocation)
        if (!path.isEmpty()) {
          for (i in 0 until path.size - 1)
            water.maxFlowOut[path[i]]--

          val sourceLocation = path[0]
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
        }
      }
    }

    // Split seas that have settled
    disturbedSeas.filterNot { it.disturbed }.forEach { water.splitSea(it) }

    // Only update water levels for actual change (rather than pass-through changes)
    return waterLevelUpdates.filter { (pos, waterLevel) -> originalWaterLevel[pos] != waterLevel }
  }

  private val waterPotentialComparator = Comparator.comparingInt(ToIntFunction<Vector3i> { pathFinder.waterPotential(it) })
}
