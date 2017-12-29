package com.fracturedskies.game.water

import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_NEG_X
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_NEG_Y
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_NEG_Z
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_X
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_Y
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_Z
import com.fracturedskies.engine.math.Vector3i.Companion.NEIGHBOURS
import com.fracturedskies.engine.math.Vector3i.Companion.XY_PLANE_NEIGHBORS
import com.fracturedskies.engine.math.Vector3i.Companion.Y_PLANE_NEIGHBORS
import java.util.*
import java.util.function.ToIntFunction

class WaterPathFinder(private val water: WaterMap){
  private data class JumpNode(val pos: Vector3i, val dirs: List<Vector3i>)
  private val waterPotentialComparator = Comparator.comparingInt(ToIntFunction<JumpNode> { waterPotential(it.pos) })

  fun find(initialPos: Vector3i): List<Vector3i> {
    val targetWaterPotential = waterPotential(initialPos) + 2

    val cameFrom = HashMap<Vector3i, Vector3i?>()
    cameFrom[initialPos] = null

    val unvisitedCells = PriorityQueue<JumpNode>(waterPotentialComparator.reversed())
    unvisitedCells.add(JumpNode(initialPos, NEIGHBOURS))

    while (!unvisitedCells.isEmpty()) {
      val sourceNode = unvisitedCells.poll()
      val sourcePos = sourceNode.pos

      // Found a source!
      if (waterPotential(sourcePos) >= targetWaterPotential) {
        var blockPos = sourcePos
        val path = ArrayList<Vector3i>()
        path.add(blockPos)
        while (cameFrom[blockPos] != null) {
          blockPos = cameFrom[blockPos]!!
          path.add(blockPos)
        }
        return path
      }

      for (successor in successors(sourceNode, targetWaterPotential)) {
        if (!cameFrom.containsKey(successor.pos)) {
          cameFrom[successor.pos] = sourceNode.pos
          unvisitedCells.add(successor)
        }
      }
    }

    // If no neighbours have higher water potential, then any higher water potential neighbours will not find any better solution either; don't waste time processing them
    for (entry in cameFrom) {
      val to = entry.key
      var from = entry.value
      if (from != null) {
        val dir = when {
          to.x > from.x -> AXIS_X
          to.x < from.x -> AXIS_NEG_X
          to.y > from.y -> AXIS_Y
          to.y < from.y -> AXIS_NEG_Y
          to.z > from.z -> AXIS_Z
          else -> AXIS_NEG_Z
        }

        while (from != to) {
          water.maxFlowOut[from] = 0
          from += dir
        }
      }
      water.maxFlowOut[to] = 0
    }
    return emptyList()
  }

  private fun successors(current: JumpNode, target: Int): List<JumpNode> {
    return current.dirs.mapNotNull { jump(current.pos, it, target) }
  }

  private tailrec fun jump(current: Vector3i, dir: Vector3i, target: Int): JumpNode? {
    val next = current + dir
    if (isBlocking(next)) return JumpNode(current, listOf())
    if (waterPotential(next) >= target)
      return JumpNode(next, listOf())
    when (dir) {
      AXIS_Y, AXIS_NEG_Y -> {
        return JumpNode(next, Vector3i.XZ_PLANE_NEIGHBORS + dir)
      }
      AXIS_X, AXIS_NEG_X -> {
        val forcedNeighbors = forcedNeighbors(current, next, Y_PLANE_NEIGHBORS)
        return JumpNode(next, Vector3i.Z_PLANE_NEIGHBORS + forcedNeighbors + dir)
      }
      AXIS_Z, AXIS_NEG_Z -> {
        val forcedNeighbors = forcedNeighbors(current, next, XY_PLANE_NEIGHBORS)
        if (forcedNeighbors.isNotEmpty())
          return JumpNode(next, forcedNeighbors + dir)
      }
    }
    return jump(next, dir, target)
  }

  fun waterPotential(pos: Vector3i) = pos.y * (WaterSystem.MAX_WATER_LEVEL + 1) + water.getLevel(pos)
  private fun forcedNeighbors(current: Vector3i, next: Vector3i, dirs: List<Vector3i>)
          = dirs.filter { isBlocking(current + it) && !isBlocking(next + it) }
  private fun isBlocking(pos: Vector3i)
          = !(water.has(pos) && water.maxFlowOut[pos] != 0.toByte())
}