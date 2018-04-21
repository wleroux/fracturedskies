package com.fracturedskies.water

import com.fracturedskies.api.*
import com.fracturedskies.engine.api.Update
import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.water.api.MAX_WATER_LEVEL
import java.util.*
import java.util.Comparator.comparingInt
import java.util.function.ToIntFunction
import kotlin.coroutines.experimental.CoroutineContext

class WaterSystem(coroutineContext: CoroutineContext) {
  private var initialized = false
  private lateinit var water: WaterMap
  private lateinit var pathFinder: WaterPathFinder
  val channel = MessageChannel(coroutineContext) { message ->
    when (message) {
      is NewGameRequested -> {
        water = WaterMap(message.dimension)
        pathFinder = WaterPathFinder(water)
        initialized = true
      }
      is WorldGenerated -> {
        if (!initialized) return@MessageChannel
        val waterLevelUpdates = mutableMapOf<Vector3i, Byte>()
        message.blocks.forEach { (blockIndex, block) ->
          val pos = message.offset + message.blocks.dimension.toVector3i(blockIndex)
          water.setOpaque(pos, block.type.opaque)
          if (block.type.opaque && water.getLevel(pos) != 0.toByte()) {
            water.setLevel(pos, 0)
            waterLevelUpdates[pos] = 0
          }
        }
        if (waterLevelUpdates.isNotEmpty()) {
          send(BlockWaterLevelUpdated(waterLevelUpdates, Cause.of(this), MultiTypeMap()))
        }
      }
      is BlockUpdated -> {
        if (!initialized) return@MessageChannel
        val waterLevelUpdates = mutableMapOf<Vector3i, Byte>()
        message.updates.forEach { (pos, type) ->
          water.setOpaque(pos, type.opaque)
          if (type.opaque && water.getLevel(pos) != 0.toByte()) {
            water.setLevel(pos, 0)
            waterLevelUpdates[pos] = 0
          }
        }
        if (waterLevelUpdates.isNotEmpty()) {
          send(BlockWaterLevelUpdated(waterLevelUpdates, Cause.of(this), MultiTypeMap()))
        }
      }
      is BlockWaterLevelUpdated -> {
        if (!initialized) return@MessageChannel
        if (message.cause.first() != this) {
          message.updates.forEach { (pos, waterLevel) ->
            water.setLevel(pos, waterLevel)
          }
        }
      }
      is Update -> {
        if (!initialized) return@MessageChannel
        val flow = flow()
        val evaporation = evaporation()

        val updates = flow + evaporation
        if (updates.isNotEmpty()) {
          send(BlockWaterLevelUpdated(updates, Cause.of(this), MultiTypeMap()))
        }
      }
    }
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

  private val waterPotentialComparator = comparingInt(ToIntFunction<Vector3i> { pathFinder.waterPotential(it) })
}
