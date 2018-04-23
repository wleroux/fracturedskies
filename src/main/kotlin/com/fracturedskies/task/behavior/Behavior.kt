package com.fracturedskies.task.behavior

import com.fracturedskies.*
import com.fracturedskies.api.ColonistMoved
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.messages.*
import com.fracturedskies.task.behavior.BehaviorStatus.*
import kotlin.coroutines.experimental.buildSequence


enum class BehaviorStatus {
  RUNNING,
  SUCCESS,
  FAILURE
}

interface Behavior {
  fun execute(state: WorldState, colonist: Colonist): Sequence<BehaviorStatus>
  fun cost(state: WorldState, colonist: Colonist): Int
  fun isPossible(state: WorldState, colonist: Colonist): Boolean
}

object NoopBehavior: Behavior {
  override fun cost(state: WorldState, colonist: Colonist) = 0
  override fun isPossible(state: WorldState, colonist: Colonist) = true
  override fun execute(state: WorldState, colonist: Colonist) = buildSequence {
    yield(SUCCESS)
  }
}

class InOrderBehavior(private vararg val behaviors: Behavior): Behavior {
  override fun cost(state: WorldState, colonist: Colonist): Int {
    var sum = 0
    for (behavior in behaviors) {
      sum += behavior.cost(state, colonist)
    }
    return sum
  }
  override fun isPossible(state: WorldState, colonist: Colonist) = behaviors.all { it.isPossible(state, colonist) }
  override fun execute(state: WorldState, colonist: Colonist) = buildSequence {
    for (behavior in behaviors) {
      loop@ for (status in behavior.execute(state, colonist)) {
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

class MoveToPositionBehavior(private vararg val positions: Vector3i): Behavior {
  override fun execute(state: WorldState, colonist: Colonist): Sequence<BehaviorStatus> = buildSequence {
    val colonistPos = colonist.position

    val path = positions
        .filter { state.blocked.has(it) }
        .filterNot { state.blocked[it] }
        .map { pos -> state.pathFinder.find(colonistPos, isTarget(pos), distanceToTarget(pos)) }
        .filter { it.isNotEmpty() }
        .minBy { it.size }
    if (path == null) {
      // If there are not possible paths, fail
      yield(FAILURE)
      return@buildSequence
    }

    for (step in path.drop(1)) {
      if (state.blocked[step]) {
        // If the path is blocked, see if we can find another
        yieldAll(MoveToPositionBehavior(*positions).execute(state, colonist))
        return@buildSequence
      } else {
        // Step towards the path
        MessageBus.send(ColonistMoved(colonist.id, step, Cause.of(this)))
        yield(RUNNING)
      }
    }
    yield(SUCCESS)
  }
  override fun cost(state: WorldState, colonist: Colonist): Int {
    val colonistPos = colonist.position
    var min = Int.MAX_VALUE
    for (pos in positions) {
      val dist = pos distanceTo colonistPos
      if (dist < min)
        min = dist
    }
    return min
  }

  private fun distanceToTarget(target: Vector3i) = { pos: Vector3i -> pos distanceTo target }
  private fun isTarget(target: Vector3i) = { pos: Vector3i -> pos == target }
  override fun isPossible(state: WorldState, colonist: Colonist) = cost(state, colonist) != Int.MAX_VALUE
}