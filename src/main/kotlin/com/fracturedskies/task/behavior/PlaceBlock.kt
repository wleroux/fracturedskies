package com.fracturedskies.task.behavior

import com.fracturedskies.*
import com.fracturedskies.api.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.task.behavior.BehaviorStatus.*
import kotlin.coroutines.experimental.buildSequence


class BehaviorPutBlock(private val pos: Vector3i, private val blockType: BlockType): Behavior {
  override fun cost(world: WorldState, colonist: Colonist) = 1
  override fun isPossible(world: WorldState, colonist: Colonist) = true
  override fun execute(world: WorldState, colonist: Colonist) = buildSequence {
    if (pos distanceTo colonist.position != 1) {
      yield(FAILURE)
    } else {
      // Look at block position
      val deltaPosition = pos - colonist.position
      if (deltaPosition.x != 0 || deltaPosition.z != 0) {
        val newDirection = (deltaPosition.toVector3() / deltaPosition.magnitude).toVector3i()
        if (newDirection != colonist.direction) {
          send(ColonistMoved(colonist.id, colonist.position, newDirection, Cause.of(this)))
          yield(RUNNING)
        }
      }

      val itemDrop = world.blockType[pos].itemDrop
      send(BlockUpdated(mapOf(pos to blockType), Cause.of(this)))
      if (itemDrop != null) {
        send(ItemSpawned(Id(), itemDrop, colonist.position, Cause.of(this)))
      }
      yield(RUNNING)
      yield(SUCCESS)
    }
  }
}

