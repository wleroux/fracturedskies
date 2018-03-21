package com.fracturedskies.water

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import java.util.*

class WaterMap(val dimension: Dimension) {
  val maxFlowOut = ByteMap(dimension)
  private val level = ByteMap(dimension)
  private val opaque = BooleanMap(dimension)
  val sea = ObjectMap<Sea?>(dimension, {null})
  val seas = mutableSetOf<Sea>()
  val evaporationCandidates = hashSetOf<Vector3i>()

  fun has(pos: Vector3i) = has(pos.x, pos.y, pos.z)
  fun has(x: Int, y: Int, z: Int) = dimension.has(x, y, z)

  class Sea {
    var providence = mutableListOf<Vector3i>()
    var disturbed: Boolean = false
    override fun toString() = "Sea[${hashCode()}]"
  }

  fun getLevel(pos: Vector3i) = this.level[pos]
  fun setLevel(pos: Vector3i, level: Byte) {
    if (this.level[pos] == level)
      return
    val seaChange = ((this.level[pos] > 0.toByte()) xor (level > 0.toByte()))
    this.level[pos] = level

    if (level == 1.toByte()) evaporationCandidates.add(pos)
    else evaporationCandidates.remove(pos)

    if (seaChange) {
      if (level > 0) {
        val commonSea = combineSeas(pos)
        // Combine common seas
        val sea = if (commonSea != null) {
          commonSea
        } else {
          val newSea = Sea()
          seas.add(newSea)
          newSea
        }

        if (this.sea[pos] == null) {
          this.sea[pos] = sea
          sea.providence.add(pos)
        }
        for (neighborPos in shore(pos)) {
          if (this.sea[neighborPos] == null) {
            this.sea[neighborPos] = sea
            sea.providence.add(neighborPos)
          }
        }
        sea.disturbed = true
      } else {
        sea[pos]?.let { disconnectFromSea(pos, it) }
      }
    }

    sea[pos]?.disturbed = true
  }

  private fun combineSeas(pos: Vector3i): Sea? {
    if (opaque[pos]) return null

    val commonSeas = mutableSetOf<Sea>()
    for (neighborPos in shore(pos)) {
      if (isWaterSource(pos) || isWaterSource(neighborPos)) {
        val neighborSea = sea[neighborPos]
        if (neighborSea != null) {
          commonSeas.add(neighborSea)
        }
      }
    }

    return if (commonSeas.isNotEmpty()) {
      val mainSea = commonSeas.first()
      for (secondarySea in commonSeas.drop(1)) {
        mainSea.providence.addAll(secondarySea.providence)
        for (waterPos in secondarySea.providence)
          sea[waterPos] = mainSea
        seas.remove(secondarySea)
        mainSea.disturbed = mainSea.disturbed || secondarySea.disturbed
      }
      mainSea
    } else {
      null
    }
  }

  fun splitSea(sea: Sea) {
    val potentialSeas = sea.providence.toHashSet()

    // Anything connected to a potential sea is shares the sea
    var first = true
    while (potentialSeas.isNotEmpty()) {
      val potentialSea = potentialSeas.first()

      val providence = connections(potentialSea)
      if (first) {
        first = false
      } else {
        // Create New Sea
        val newSea = Sea()
        sea.providence.removeAll(providence)
        newSea.providence.addAll(providence)
        for (waterPos in providence)
          this.sea[waterPos] = newSea
        seas.add(newSea)
      }
      potentialSeas.removeAll(providence)
    }
  }

  private fun connections(pos: Vector3i): Set<Vector3i> {
    val connections = hashSetOf<Vector3i>()
    connections.add(pos)

    val unvisited = LinkedList<Vector3i>()
    unvisited.add(pos)
    while (unvisited.isNotEmpty()) {
      val visitPos = unvisited.pollFirst()
      for (neighborVec in Vector3i.NEIGHBOURS) {
        val neighborPos = visitPos + neighborVec
        if (!has(neighborPos)) continue
        if (opaque[neighborPos]) continue

        if (!connections.contains(neighborPos)) {
          if (isWaterSource(visitPos) || isWaterSource(neighborPos)) {
            connections.add(neighborPos)
            unvisited.add(neighborPos)
          }
        }
      }
    }

    return connections
  }

  fun shore(pos: Vector3i) = Vector3i.NEIGHBOURS.map { pos + it }.filter { has(it) && !opaque[it] }
  fun setOpaque(pos: Vector3i, value: Boolean) {
    opaque[pos] = value
    if (!value) {
      sea[pos] = combineSeas(pos)
      sea[pos]?.providence?.add(pos)
      sea[pos]?.disturbed = true
    } else {
      sea[pos]?.let { disconnectFromSea(pos, it) }
    }
  }

  private fun disconnectFromSea(pos: Vector3i, sea: Sea) {
    for (shorePos in (shore(pos) + pos)) {
      if (!isWaterSource(shorePos)) {
        if (opaque[shorePos] || shore(shorePos).all { !isWaterSource(it) }) {
          sea.providence.remove(shorePos)
          this.sea[shorePos] = null
        }
      }
    }
    if (sea.providence.isEmpty()) seas.remove(sea)
  }

  private fun isWaterSource(pos: Vector3i) = level[pos] > 0 && !opaque[pos]
}