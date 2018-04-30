package com.fracturedskies.engine.math

import com.fracturedskies.api.block.Block
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
typealias CostHeuristic = (cost: Int, pos: Vector3i) -> Int
typealias IsTraversable = (fromPos: Vector3i, toPos: Vector3i) -> Boolean

class PathFinder(private val isTraversable: IsTraversable){
  companion object {
    fun isNotOpaque(blocks: ObjectSpace<Block>): IsTraversable = { _, toPos -> blocks.has(toPos) && !blocks[toPos].type.opaque }
    fun target(target: Vector3i): IsTarget = {pos: Vector3i -> pos == target }
    fun targets(targets: List<Vector3i>): IsTarget = {pos: Vector3i -> targets.contains(pos) }
    fun targetDistanceHeuristic(target: Vector3i): CostHeuristic = {cost: Int, pos: Vector3i -> cost + (pos distanceTo target) }
    fun targetsDistanceHeuristic(targets: List<Vector3i>): CostHeuristic = { cost: Int, pos: Vector3i -> cost + (targets.map { pos distanceTo it }.min() ?: Int.MAX_VALUE) }
  }

  data class SearchResult(val path: List<Vector3i>, val cameFrom: Map<Vector3i, Vector3i?>)

  private data class JumpNode(val pos: Vector3i, val dirs: List<Vector3i>, val cost: Int)

  fun find(initialPos: Vector3i, isTarget: IsTarget, costHeuristic: CostHeuristic, jumpPointsOnly: Boolean = false): SearchResult {
    val cameFrom = HashMap<Vector3i, Vector3i?>()
    cameFrom[initialPos] = null

    val costComparator = Comparator.comparing { it: JumpNode -> costHeuristic(it.cost, it.pos) }
    val unvisitedCells = PriorityQueue<JumpNode>(costComparator)
    unvisitedCells.add(JumpNode(initialPos, NEIGHBOURS, 0))

    while (!unvisitedCells.isEmpty()) {
      val node = unvisitedCells.poll()
      val nodePos = node.pos

      // Found the target!
      if (isTarget(nodePos)) {
        var currPos = nodePos

        val path = LinkedList<Vector3i>()
        if (!jumpPointsOnly) {
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
              path.addFirst(currPos)
              currPos -= dir
            }
          }
          path.addFirst(initialPos)
        } else {
          while (cameFrom[currPos] != null) {
            path.addFirst(currPos)
            currPos = cameFrom[currPos]!!
          }
          path.addFirst(initialPos)
        }
        return SearchResult(path, cameFrom)
      }

      for (successor in successors(node, isTarget, node.cost)) {
        if (!cameFrom.containsKey(successor.pos)) {
          cameFrom[successor.pos] = node.pos
          unvisitedCells.add(successor)
        }
      }
    }

    return SearchResult(emptyList(), cameFrom)
  }

  private fun successors(current: JumpNode, isTarget: IsTarget, cost: Int): List<JumpNode> {
    val jumpPoints = mutableListOf<JumpNode>()
    current.dirs.forEach { jump(current.pos, it, isTarget, cost, jumpPoints) }
    return jumpPoints
  }

  private tailrec fun jump(current: Vector3i, dir: Vector3i, isTarget: IsTarget, cost: Int, jumpPoints: MutableList<JumpNode>) {
    val next = current + dir
    if (!isTraversable(current, next)) return
    if (isTarget(next)) {
      jumpPoints += JumpNode(next, listOf(), cost + 1)
      return
    }
    when (dir) {
      AXIS_Y, AXIS_NEG_Y -> {
        jumpPoints += JumpNode(next, Vector3i.XZ_PLANE_NEIGHBORS, cost + 1)
      }
      AXIS_X, AXIS_NEG_X -> {
        val forcedNeighbors = forcedNeighbors(current, next, Y_PLANE_NEIGHBORS)
        jumpPoints += JumpNode(next, Vector3i.Z_PLANE_NEIGHBORS + forcedNeighbors, cost + 1)
      }
      AXIS_Z, AXIS_NEG_Z -> {
        val forcedNeighbors = forcedNeighbors(current, next, XY_PLANE_NEIGHBORS)
        if (forcedNeighbors.isNotEmpty())
          jumpPoints += JumpNode(next, forcedNeighbors, cost + 1)
      }
    }
    return jump(next, dir, isTarget, cost + 1, jumpPoints)
  }
  private fun forcedNeighbors(current: Vector3i, next: Vector3i, dirs: List<Vector3i>)
          = dirs.filter { !isTraversable(current, current + it) && isTraversable(next, next + it) }
}