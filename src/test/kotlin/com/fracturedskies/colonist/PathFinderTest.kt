package com.fracturedskies.colonist

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.PathFinder.Companion.target
import com.fracturedskies.engine.math.PathFinder.Companion.targetDistanceHeuristic
import org.junit.Test
import kotlin.test.assertEquals


class PathFinderTest {
  private val blocked = BooleanMutableSpace(Dimension(8, 8, 8))
  private val pathFinder = PathFinder({ _, toPos -> blocked.has(toPos) && !blocked[toPos]})

  @Test
  fun itFindsPath() {
    val targetPos = Vector3i(7, 0, 0)
    val path = pathFinder.find(Vector3i(0, 0, 0), target(targetPos), targetDistanceHeuristic(targetPos))

    assertEquals(listOf(
        Vector3i(0, 0, 0),
        Vector3i(1, 0, 0),
        Vector3i(2, 0, 0),
        Vector3i(3, 0, 0),
        Vector3i(4, 0, 0),
        Vector3i(5, 0, 0),
        Vector3i(6, 0, 0),
        Vector3i(7, 0, 0)
    ), path)
  }

  @Test
  fun itDoesNotGoThroughBlocks() {
    blocked[Vector3i(3, 0, 0)] = true
    val targetPos = Vector3i(7, 0, 0)
    val path = pathFinder.find(Vector3i(0, 0, 0), target(targetPos), targetDistanceHeuristic(targetPos))

    assertEquals(listOf(
        Vector3i(0, 0, 0),
        Vector3i(1, 0, 0),
        Vector3i(2, 0, 0),
        Vector3i(2, 0, 1),
        Vector3i(3, 0, 1),
        Vector3i(4, 0, 1),
        Vector3i(5, 0, 1),
        Vector3i(6, 0, 1),
        Vector3i(7, 0, 1),
        Vector3i(7, 0, 0)
    ), path)
  }
}