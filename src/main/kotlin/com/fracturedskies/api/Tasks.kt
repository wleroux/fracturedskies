package com.fracturedskies.api

import com.fracturedskies.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.task.api.*
import com.fracturedskies.task.api.TaskCategory.*
import com.fracturedskies.task.api.TaskPriority.AVERAGE
import com.fracturedskies.task.behavior.*
import com.fracturedskies.task.behavior.BehaviorStatus.*
import kotlin.coroutines.experimental.buildSequence

abstract class TaskType {
  abstract val behavior: Behavior
  abstract val category: TaskCategory
  open val condition: Condition = AllOfCondition()

  override fun toString(): String {
    return javaClass.simpleName
  }
}

class TaskPlaceBlock(pos: Vector3i, blockType: BlockType): TaskType() {
  override val category: TaskCategory = CONSTRUCTION
  override val condition: Condition = SingleAssigneeCondition
  override val behavior = BehaviorInOrder(
      BehaviorMoveToPosition({ _, _ -> Vector3i.NEIGHBOURS.map { pos + it }}),
      BehaviorPutBlock(pos, blockType)
  )
}
class TaskRemoveBlock(pos: Vector3i): TaskType() {
  override val category: TaskCategory = MINE
  override val behavior = BehaviorInOrder(
      BehaviorMoveToPosition({ _, _ -> Vector3i.NEIGHBOURS.map { pos + it }}),
      BehaviorPutBlock(pos, BlockAir)
  )
  override val condition: Condition = SingleAssigneeCondition
}

class TaskPickItem(itemId: Id): TaskType() {
  override val category: TaskCategory = HAUL
  override val condition: Condition = AllOfCondition(
      SingleAssigneeCondition,
      InventoryIsNotFull,
      DepositZoneExists
  )
  override val behavior = BehaviorInOrder(
      BehaviorMoveToPosition({ world, _ ->
        val item = world.items[itemId]!!
        if (item.position != null) listOf(item.position!!) else emptyList()
      }),
      BehaviorPickItem(itemId)
  )
}
const val MAX_INVENTORY_SIZE = 10
object InventoryIsNotFull: Condition {
  override fun matches(world: WorldState, colonist: Colonist, task: Task): Boolean {
    return colonist.inventory.size < MAX_INVENTORY_SIZE
  }
}

class BehaviorPickItem(private val itemId: Id): Behavior {
  override fun cost(world: WorldState, colonist: Colonist) = 1
  override fun isPossible(world: WorldState, colonist: Colonist): Boolean {
    return colonist.inventory.contains(itemId) || world.items[itemId]!!.position != null
  }
  override fun execute(world: WorldState, colonist: Colonist) = buildSequence {
    if (colonist.inventory.contains(itemId)) {
      yield(SUCCESS)
    } else {
      val item = world.items[itemId]!!
      when {
        item.position == null -> yield(FAILURE)
        item.position!! distanceTo colonist.position == 0 -> {
          send(ColonistPickedItem(colonist.id, itemId, Cause.of(this)))
          send(TaskCreated(Id(), TaskDepositItem(colonist.id, itemId), AVERAGE, Cause.of(this)))
          yield(RUNNING)
          yield(SUCCESS)
        }
        else -> yield(FAILURE)
      }
    }
  }
}

class TaskDepositItem(colonistId: Id, itemId: Id): TaskType() {
  override val category: TaskCategory = HAUL
  override val condition: Condition = AllOfCondition(
      SpecificColonistCondition(colonistId),
      DepositZoneExists
  )
  override val behavior = BehaviorInOrder(
      BehaviorMoveToPosition({ world, _ ->
        world.zones.flatMap { it.value.positions }
      }),
      DropItem(itemId)
  )
}

object DepositZoneExists: Condition {
  override fun matches(world: WorldState, colonist: Colonist, task: Task): Boolean {
    return world.zones.isNotEmpty()
  }
}

class DropItem(private val itemId: Id): Behavior {
  override fun cost(world: WorldState, colonist: Colonist) = 1
  override fun isPossible(world: WorldState, colonist: Colonist) = colonist.inventory.isNotEmpty()
  override fun execute(world: WorldState, colonist: Colonist) = buildSequence {
    if (colonist.inventory.contains(itemId)) {
      send(ColonistDroppedItem(colonist.id, itemId, Cause.of(this)))
      yield(RUNNING)
      yield(SUCCESS)
    } else {
      yield(FAILURE)
    }
  }
}