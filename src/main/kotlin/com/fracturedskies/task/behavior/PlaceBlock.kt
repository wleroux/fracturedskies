package com.fracturedskies.task.behavior

import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.task.*
import com.fracturedskies.task.behavior.BehaviorStatus.*
import kotlin.coroutines.experimental.buildSequence


fun placeBlock(pos: Vector3i, blockType: BlockType) = InOrderBehavior(
    MoveToPositionBehavior(*(Vector3i.NEIGHBOURS.map { pos + it }.toTypedArray())),
    PutBlockBehavior(pos, blockType)
)

fun removeBlock(pos: Vector3i) = InOrderBehavior(
    MoveToPositionBehavior(*(Vector3i.NEIGHBOURS.map { pos + it }.toTypedArray())),
    PutBlockBehavior(pos, AIR)
)

class PutBlockBehavior(private val pos: Vector3i, private val blockType: BlockType): Behavior {
  override fun cost(state: WorldState, colonist: Colonist) = 1
  override fun isPossible(state: WorldState, colonist: Colonist) = true
  override fun execute(state: WorldState, colonist: Colonist) = buildSequence {
    if (pos distanceTo colonist.position != 1) {
      yield(FAILURE)
    } else {
      val previousBlockType = state.blockType[pos]
      send(BlockUpdated(mapOf(pos to blockType), Cause.of(this)))
      if (previousBlockType != BlockType.AIR) {
        send(ItemSpawned(Id(), previousBlockType, colonist.position, Cause.of(this)))
      }
      yield(RUNNING)
      yield(SUCCESS)
    }
  }
}

