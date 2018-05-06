package com.fracturedskies.api.task

import com.fracturedskies.api.World
import com.fracturedskies.api.block.*
import com.fracturedskies.api.entity.Colonist
import com.fracturedskies.api.task.BehaviorStatus.*
import com.fracturedskies.api.task.TaskCategory.*
import com.fracturedskies.api.task.TaskPriority.AVERAGE
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.api.Cause
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_NEG_Y
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
      BehaviorMoveToPosition({ _, _ -> Vector3i.NEIGHBOURS.map { pos + it } }),
      BehaviorPutBlock(pos, blockType)
  )
}
class TaskRemoveBlock(pos: Vector3i): TaskType() {
  override val category: TaskCategory = MINE
  override val behavior = BehaviorInOrder(
      BehaviorMoveToPosition({ _, _ -> Vector3i.NEIGHBOURS.map { pos + it } }),
      BehaviorPutBlock(pos, BlockTypeAir)
  )
  override val condition: Condition = SingleAssigneeCondition
}

class TaskPickItem(itemId: Id): TaskType() {
  override val category: TaskCategory = HAUL
  override val condition: Condition = AllOfCondition(
      SingleAssigneeCondition,
      InventoryIsNotFull,
      DepositZoneExistsCondition
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
  override fun matches(world: World, colonist: Colonist, task: Task): Boolean {
    return colonist.inventory.size < MAX_INVENTORY_SIZE
  }
}

class BehaviorPickItem(private val itemId: Id): Behavior {
  override fun cost(world: World, colonist: Colonist) = 1
  override fun isPossible(world: World, colonist: Colonist): Boolean {
    return colonist.inventory.contains(itemId) || world.items[itemId]!!.position != null
  }
  override fun execute(world: World, colonist: Colonist) = buildSequence {
    if (colonist.inventory.contains(itemId)) {
      yield(SUCCESS)
    } else {
      val item = world.items[itemId]!!
      when {
        item.position == null -> yield(FAILURE)
        item.position!! distanceTo colonist.position == 0 -> {
          world.pickItem(colonist.id, itemId, Cause.of(this))
          world.createTask(Id(), TaskDepositItem(colonist.id, itemId), AVERAGE, Cause.of(this))
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
      DepositZoneExistsCondition
  )
  override val behavior = BehaviorInOrder(
      BehaviorMoveToPosition({ world, _ ->
        world.zones.values
            .flatMap { it.positions }
            .filter { world.blocks[it].type == BlockTypeAir }
            .filter { world.has(it + AXIS_NEG_Y) && world.blocks[it + AXIS_NEG_Y].type != BlockTypeAir }
      }),
      BehaviorDropItem(itemId)
  )
}

object DepositZoneExistsCondition: Condition {
  override fun matches(world: World, colonist: Colonist, task: Task): Boolean {
    return world.zones.values.firstOrNull {
      it.positions
          .filter { world.blocks[it].type == BlockTypeAir }
          .firstOrNull { world.has(it + AXIS_NEG_Y) && world.blocks[it + AXIS_NEG_Y].type != BlockTypeAir } != null
    } != null
  }
}

class BehaviorDropItem(private val itemId: Id): Behavior {
  override fun cost(world: World, colonist: Colonist) = 1
  override fun isPossible(world: World, colonist: Colonist) = true
  override fun execute(world: World, colonist: Colonist) = buildSequence {
    if (colonist.inventory.contains(itemId)) {
      world.dropItem(colonist.id, itemId, Cause.of(this))
      yield(SUCCESS)
    } else {
      yield(FAILURE)
    }
  }
}