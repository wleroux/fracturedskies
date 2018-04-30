package com.fracturedskies.api.task

import com.fracturedskies.api.World
import com.fracturedskies.api.block.*
import com.fracturedskies.api.entity.Colonist
import com.fracturedskies.api.task.BehaviorStatus.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.api.Cause
import com.fracturedskies.engine.math.*
import kotlin.coroutines.experimental.buildSequence


class BehaviorPutBlock(private val pos: Vector3i, private val blockType: BlockType): Behavior {
  override fun cost(world: World, colonist: Colonist) = 1
  override fun isPossible(world: World, colonist: Colonist) = true
  override fun execute(world: World, colonist: Colonist) = buildSequence {
    if (pos distanceTo colonist.position != 1) {
      yield(FAILURE)
    } else {
      // Look at block position
      val deltaPosition = pos - colonist.position
      if (deltaPosition.x != 0 || deltaPosition.z != 0) {
        val newDirection = (deltaPosition.toVector3() / deltaPosition.magnitude).toVector3i()
        if (newDirection != colonist.direction) {
          world.moveColonist(colonist.id, colonist.position, newDirection, Cause.of(this))
          yield(RUNNING)
        }
      }

      val prevBlock = world.blocks[pos].type
      val itemDrop = prevBlock.itemDrop
      world.updateBlock(pos, Block(blockType), Cause.of(this))
      if (itemDrop != null) {
        world.spawnItem(Id(), itemDrop, colonist.position, Cause.of(this))
      }
      yield(RUNNING)
      yield(SUCCESS)
    }
  }
}

