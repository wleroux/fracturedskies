package com.fracturedskies.task.behavior

import com.fracturedskies.*
import com.fracturedskies.api.ColonistMoved
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.PathFinder.Companion.targets
import com.fracturedskies.engine.math.PathFinder.Companion.targetsDistanceHeuristic
import com.fracturedskies.engine.messages.*
import com.fracturedskies.task.behavior.BehaviorStatus.*
import kotlin.coroutines.experimental.buildSequence


enum class BehaviorStatus {
  RUNNING,
  SUCCESS,
  FAILURE
}

interface Behavior {
  fun execute(world: WorldState, colonist: Colonist): Sequence<BehaviorStatus>
  fun cost(world: WorldState, colonist: Colonist): Int
  fun isPossible(world: WorldState, colonist: Colonist): Boolean
}

object BehaviorSuccess: Behavior {
  override fun cost(world: WorldState, colonist: Colonist) = 0
  override fun isPossible(world: WorldState, colonist: Colonist) = true
  override fun execute(world: WorldState, colonist: Colonist) = buildSequence {
    yield(SUCCESS)
  }
}

class BehaviorInOrder(private vararg val behaviors: Behavior): Behavior {
  override fun cost(world: WorldState, colonist: Colonist): Int {
    var sum = 0
    for (behavior in behaviors) {
      sum += behavior.cost(world, colonist)
    }
    return sum
  }
  override fun isPossible(world: WorldState, colonist: Colonist) = behaviors.all { it.isPossible(world, colonist) }
  override fun execute(world: WorldState, colonist: Colonist) = buildSequence {
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

class BehaviorMoveToPosition(private val positions: (WorldState, Colonist) -> List<Vector3i>): Behavior {
  override fun execute(world: WorldState, colonist: Colonist): Sequence<BehaviorStatus> = buildSequence {
    val colonistPos = colonist.position

    val path = world.pathFinder.find(colonistPos, targets(positions(world, colonist)), targetsDistanceHeuristic(positions(world, colonist))).path
    if (path.isEmpty()) {
      // If there are no possible paths, fail
      yield(FAILURE)
      return@buildSequence
    }

    for (step in path.drop(1)) {
      if (world.blocked[step]) {
        // If the path is blocked, see if we can find another
        yieldAll(BehaviorMoveToPosition(positions).execute(world, colonist))
        return@buildSequence
      } else {
        // Step towards the path
        val deltaPosition = step - colonist.position
        val newDirection = if (deltaPosition.x != 0 || deltaPosition.z != 0)
          (deltaPosition.toVector3() / deltaPosition.magnitude).toVector3i()
        else colonist.direction

        MessageBus.send(ColonistMoved(colonist.id, step, newDirection, Cause.of(this)))
        yield(RUNNING)
      }
    }
    yield(SUCCESS)
  }
  override fun cost(world: WorldState, colonist: Colonist): Int {
    val colonistPos = colonist.position
    var min = Int.MAX_VALUE
    for (pos in positions(world, colonist)) {
      val dist = pos distanceTo colonistPos
      if (dist < min)
        min = dist
    }
    return min
  }

  override fun isPossible(world: WorldState, colonist: Colonist) = cost(world, colonist) != Int.MAX_VALUE
}