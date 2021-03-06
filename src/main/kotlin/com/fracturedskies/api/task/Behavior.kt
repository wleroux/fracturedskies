package com.fracturedskies.api.task

import com.fracturedskies.api.World
import com.fracturedskies.api.entity.Colonist
import com.fracturedskies.api.task.BehaviorStatus.*
import com.fracturedskies.engine.api.Cause
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.PathFinder.Companion.targets
import com.fracturedskies.engine.math.PathFinder.Companion.targetsDistanceHeuristic
import kotlin.coroutines.experimental.buildSequence


enum class BehaviorStatus {
  RUNNING,
  SUCCESS,
  FAILURE
}

interface Behavior {
  fun execute(world: World, colonist: Colonist): Sequence<BehaviorStatus>
  fun cost(world: World, colonist: Colonist): Int
  fun isPossible(world: World, colonist: Colonist): Boolean
}

object BehaviorSuccess: Behavior {
  override fun cost(world: World, colonist: Colonist) = 0
  override fun isPossible(world: World, colonist: Colonist) = true
  override fun execute(world: World, colonist: Colonist) = buildSequence {
    yield(SUCCESS)
  }
}

class BehaviorInOrder(private vararg val behaviors: Behavior): Behavior {
  override fun cost(world: World, colonist: Colonist): Int {
    var sum = 0
    for (behavior in behaviors) {
      sum += behavior.cost(world, colonist)
    }
    return sum
  }
  override fun isPossible(world: World, colonist: Colonist) = behaviors.all { it.isPossible(world, colonist) }
  override fun execute(world: World, colonist: Colonist) = buildSequence {
    for (behavior in behaviors) {
      loop@ for (status in behavior.execute(world, colonist)) {
        when (status) {
          SUCCESS -> {
            continue@loop
          }
          RUNNING -> {
            yield(RUNNING)
            continue@loop
          }
          FAILURE -> {
            yield(FAILURE)
            return@buildSequence
          }
        }
      }
    }
    yield(SUCCESS)
  }
}

class BehaviorMoveToPosition(private val positions: (World, Colonist) -> List<Vector3i>): Behavior {
  override fun execute(world: World, colonist: Colonist): Sequence<BehaviorStatus> = buildSequence {
    val colonistPos = colonist.position

    val targetPositions = positions(world, colonist).filter(world::has).filter { !world.blocks[it].type.opaque }
    if (targetPositions.isEmpty()) {
      yield(FAILURE)
      return@buildSequence
    }

    val path = world.pathFinder.find(colonistPos, targets(positions(world, colonist)), targetsDistanceHeuristic(targetPositions)).path
    if (path.isEmpty()) {
      // If there are no possible paths, fail
      yield(FAILURE)
      return@buildSequence
    }

    for (step in path.drop(1)) {
      if (world.blocks[step].type.opaque) {
        // If the path is blocked, see if we can find another
        yieldAll(BehaviorMoveToPosition(positions).execute(world, colonist))
        return@buildSequence
      } else {
        // Step towards the path
        val deltaPosition = step - colonist.position
        val newDirection = if (deltaPosition.x != 0 || deltaPosition.z != 0)
          (deltaPosition.toVector3() / deltaPosition.magnitude).toVector3i()
        else colonist.direction

        world.moveColonist(colonist.id, step, newDirection, Cause.of(this))
        yield(RUNNING)
      }
    }
    yield(SUCCESS)
  }
  override fun cost(world: World, colonist: Colonist): Int {
    val colonistPos = colonist.position
    var min = Int.MAX_VALUE
    val targetPositions = positions(world, colonist)
        .filter(world::has)
        .filter { !world.blocks[it].type.opaque }
    for (pos in targetPositions) {
      val dist = pos distanceTo colonistPos
      if (dist < min)
        min = dist
    }
    return min
  }

  override fun isPossible(world: World, colonist: Colonist) = cost(world, colonist) != Int.MAX_VALUE
}