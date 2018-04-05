package com.fracturedskies.engine.math

import com.fracturedskies.engine.collections.BooleanSpace
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

class PathFinder(private val blockedSpace: BooleanSpace){
  private data class JumpNode(val pos: Vector3i, val dirs: List<Vector3i>)
  private fun distance(from: Vector3i, to: Vector3i) =
    (to - from).toVector3().magnitude

  fun find(initialPos: Vector3i, targetPos: Vector3i): List<Vector3i> {
    val cameFrom = HashMap<Vector3i, Vector3i?>()
    cameFrom[initialPos] = null

    val distanceComparator = Comparator.comparing { it: JumpNode ->
      distance(it.pos, targetPos)
    }

    val unvisitedCells = PriorityQueue<JumpNode>(distanceComparator)
    unvisitedCells.add(JumpNode(initialPos, NEIGHBOURS))

    while (!unvisitedCells.isEmpty()) {
      val node = unvisitedCells.poll()
      val nodePos = node.pos

      // Found the target!
      if (nodePos == targetPos) {
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
        return path.reversed()
      }

      for (successor in successors(node, targetPos)) {
        if (!cameFrom.containsKey(successor.pos)) {
          cameFrom[successor.pos] = node.pos
          unvisitedCells.add(successor)
        }
      }
    }

    return emptyList()
  }

  private fun successors(current: JumpNode, target: Vector3i): List<JumpNode> {
    return current.dirs.map { jump(current.pos, it, target) }
  }

  private tailrec fun jump(current: Vector3i, dir: Vector3i, target: Vector3i): JumpNode {
    val next = current + dir
    if (isBlocking(next)) return JumpNode(current, listOf())
    if (next == target)
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

  private fun forcedNeighbors(current: Vector3i, next: Vector3i, dirs: List<Vector3i>)
          = dirs.filter { isBlocking(current + it) && !isBlocking(next + it) }
  private fun isBlocking(pos: Vector3i)
          = !(blockedSpace.has(pos) && !blockedSpace[pos])
}