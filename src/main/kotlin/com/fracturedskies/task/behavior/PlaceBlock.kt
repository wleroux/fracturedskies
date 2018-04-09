package com.fracturedskies.task.behavior

import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.math.Vector3i
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
    send(BlockUpdated(mapOf(pos to blockType), Cause.of(this)))
    yield(RUNNING)
    yield(SUCCESS)
  }
}

