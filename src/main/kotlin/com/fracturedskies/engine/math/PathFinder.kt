package com.fracturedskies.engine.math

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
import java.util.function.*

class PathFinder(private val isBlocked: Predicate<Vector3i>){
  private data class JumpNode(val pos: Vector3i, val dirs: List<Vector3i>)

  fun find(initialPos: Vector3i, isTarget: Predicate<Vector3i>, costHeuristic: ToIntFunction<Vector3i>): List<Vector3i>? {
    val cameFrom = HashMap<Vector3i, Vector3i?>()
    cameFrom[initialPos] = null

    val costComparator = Comparator.comparing { it: JumpNode -> costHeuristic.applyAsInt(it.pos) }
    val unvisitedCells = PriorityQueue<JumpNode>(costComparator)
    unvisitedCells.add(JumpNode(initialPos, NEIGHBOURS))

    while (!unvisitedCells.isEmpty()) {
      val node = unvisitedCells.poll()
      val nodePos = node.pos

      // Found the target!
      if (isTarget.test(nodePos)) {
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

      for (successor in successors(node, isTarget)) {
        if (!cameFrom.containsKey(successor.pos)) {
          cameFrom[successor.pos] = node.pos
          unvisitedCells.add(successor)
        }
      }
    }

    return null
  }

  private fun successors(current: JumpNode, isTarget: Predicate<Vector3i>) =
    current.dirs.map { jump(current.pos, it, isTarget) }

  private tailrec fun jump(current: Vector3i, dir: Vector3i, isTarget: Predicate<Vector3i>): JumpNode {
    val next = current + dir
    return when {
      isBlocked.test(next) -> JumpNode(current, listOf())
      isTarget.test(next) -> JumpNode(next, listOf())
      else -> when (dir) {
        AXIS_Y, AXIS_NEG_Y -> {
          JumpNode(next, Vector3i.XZ_PLANE_NEIGHBORS + dir)
        }
        AXIS_X, AXIS_NEG_X -> {
          val forcedNeighbors = forcedNeighbors(current, next, Y_PLANE_NEIGHBORS)
          JumpNode(next, Vector3i.Z_PLANE_NEIGHBORS + forcedNeighbors + dir)
        }
        AXIS_Z, AXIS_NEG_Z -> {
          val forcedNeighbors = forcedNeighbors(current, next, XY_PLANE_NEIGHBORS)
          if (forcedNeighbors.isNotEmpty())
            JumpNode(next, forcedNeighbors + dir)
          else jump(next, dir, isTarget)
        }
        else -> jump(next, dir, isTarget)
      }
    }
  }
  private fun forcedNeighbors(current: Vector3i, next: Vector3i, dirs: List<Vector3i>)
          = dirs.filter { isBlocked.test(current + it) && !isBlocked.test(next + it) }
}