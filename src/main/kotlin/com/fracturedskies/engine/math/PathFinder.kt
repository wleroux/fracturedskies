package com.fracturedskies.engine.math

import com.fracturedskies.Block
import com.fracturedskies.engine.collections.ObjectSpace
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

typealias IsTarget = (pos: Vector3i) -> Boolean
typealias CostHeuristic = (pos: Vector3i) -> Int
typealias IsTraversable = (fromPos: Vector3i, toPos: Vector3i) -> Boolean

class PathFinder(private val isTraversable: IsTraversable){
  companion object {
    fun isNotOpaque(blocks: ObjectSpace<Block>): IsTraversable = { _, toPos -> blocks.has(toPos) && !blocks[toPos].type.opaque }
    fun target(target: Vector3i): IsTarget = {pos: Vector3i -> pos == target }
    fun targets(targets: List<Vector3i>): IsTarget = {pos: Vector3i -> targets.contains(pos) }
    fun targetDistanceHeuristic(target: Vector3i): CostHeuristic = {pos: Vector3i -> pos distanceTo target }
    fun targetsDistanceHeuristic(targets: List<Vector3i>): CostHeuristic = { pos: Vector3i -> targets.map { pos distanceTo it }.min() ?: Int.MAX_VALUE }
  }

  private data class JumpNode(val pos: Vector3i, val dirs: List<Vector3i>, val cost: Int)

  fun find(initialPos: Vector3i, isTarget: IsTarget, costHeuristic: CostHeuristic): List<Vector3i> {
    val cameFrom = HashMap<Vector3i, Vector3i?>()
    cameFrom[initialPos] = null

    val costComparator = Comparator.comparing { it: JumpNode -> it.cost + costHeuristic(it.pos) }
    val unvisitedCells = PriorityQueue<JumpNode>(costComparator)
    unvisitedCells.add(JumpNode(initialPos, NEIGHBOURS, 0))

    while (!unvisitedCells.isEmpty()) {
      val node = unvisitedCells.poll()
      val nodePos = node.pos

      // Found the target!
      if (isTarget(nodePos)) {
        var currPos = nodePos

        val path = ArrayList<Vector3i>()
        while (cameFrom[currPos] != null) {
          val prevPos = cameFrom[currPos]!!
          val dir = when {
            currPos.x > prevPos.x -> AXIS_X
            currPos.x < prevPos.x -> AXIS_NEG_X
            currPos.y > prevPos.y -> AXIS_Y
            currPos.y < prevPos.y -> AXIS_NEG_Y
            currPos.z > prevPos.z -> AXIS_Z
            else -> AXIS_NEG_Z
          }
          while (prevPos != currPos) {
            path.add(currPos)
            currPos -= dir
          }
        }
        path.add(initialPos)
        return path.reversed()
      }

      for (successor in successors(node, isTarget, node.cost)) {
        if (!cameFrom.containsKey(successor.pos)) {
          cameFrom[successor.pos] = node.pos
          unvisitedCells.add(successor)
        }
      }
    }

    return emptyList()
  }

  private fun successors(current: JumpNode, isTarget: IsTarget, cost: Int) =
    current.dirs.map { jump(current.pos, it, isTarget, cost) }

  private tailrec fun jump(current: Vector3i, dir: Vector3i, isTarget: IsTarget, cost: Int): JumpNode {
    val next = current + dir
    return when {
      !isTraversable(current, next) -> JumpNode(current, listOf(), cost)
      isTarget(next) -> JumpNode(next, listOf(), cost + 1)
      else -> when (dir) {
        AXIS_Y, AXIS_NEG_Y -> {
          JumpNode(next, Vector3i.XZ_PLANE_NEIGHBORS + dir, cost + 1)
        }
        AXIS_X, AXIS_NEG_X -> {
          val forcedNeighbors = forcedNeighbors(current, next, Y_PLANE_NEIGHBORS)
          JumpNode(next, Vector3i.Z_PLANE_NEIGHBORS + forcedNeighbors + dir, cost + 1)
        }
        AXIS_Z, AXIS_NEG_Z -> {
          val forcedNeighbors = forcedNeighbors(current, next, XY_PLANE_NEIGHBORS)
          if (forcedNeighbors.isNotEmpty())
            JumpNode(next, forcedNeighbors + dir, cost + 1)
          else jump(next, dir, isTarget, cost + 1)
        }
        else -> jump(next, dir, isTarget, cost + 1)
      }
    }
  }
  private fun forcedNeighbors(current: Vector3i, next: Vector3i, dirs: List<Vector3i>)
          = dirs.filter { !isTraversable(current, current + it) && isTraversable(next, next + it) }
}