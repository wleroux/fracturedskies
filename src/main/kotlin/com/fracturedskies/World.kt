package com.fracturedskies

import com.fracturedskies.api.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.task.api.*
import com.fracturedskies.task.api.TaskPriority.AVERAGE


class Colonist(
    val id: Id,
    var position: Vector3i,
    var direction: Vector3i,
    val categoryPriorities: MutableMap<TaskCategory, TaskPriority>,
    val rejectedTasks: MutableMap<Id, Int>,
    val inventory: MutableList<Id>,
    var assignedTask: Id?
) {
  override fun hashCode() = id.hashCode()
  override fun equals(other: Any?) = when (other) {
    is Colonist -> id == other.id
    else -> false
  }

  fun process(state: World, message: Any) {
    when (message) {
      is ColonistMoved -> if (message.id == id) {
        direction = message.direction
        position = message.pos
      }
      is ColonistTaskSelected -> if (message.colonist == id) { assignedTask = message.task }
      is ColonistRejectedTask -> {
        if (message.taskId == assignedTask) assignedTask = null
        if (message.colonistId == id) rejectedTasks[message.taskId] = (rejectedTasks[message.taskId] ?: 0) + 1
      }
      is ColonistPickedItem -> if (message.colonistId == id) {
        inventory += message.itemId
      }
      is ColonistDroppedItem -> if (message.colonistId == id) {
        inventory -= message.itemId
      }
      is TaskCompleted -> if (assignedTask == message.id) { assignedTask = null }
      is TaskCancelled -> if (assignedTask == message.id) { assignedTask = null }
    }
  }
}

class Task(
    val id: Id,
    val details: TaskType,
    var priority: TaskPriority = AVERAGE,
    var assigned: List<Id>
) {
  override fun hashCode() = id.hashCode()
  override fun equals(other: Any?) = when(other) {
    is Task -> id == other.id
    else -> false
  }

  override fun toString(): String {
    return "Task[id=$id; details=$details; priority=$priority; assigned=$assigned]"
  }

  fun process(state: World, message: Any) {
    when (message) {
      is ColonistRejectedTask -> {
        if (message.taskId == id)
          assigned -= message.colonistId
      }
      is ColonistTaskSelected -> {
        if (message.task == id) {
          assigned += message.colonist
        } else {
          assigned -= message.colonist
        }
      }
    }
  }
}

class Item(
    val id: Id,
    var position: Vector3i?,
    var colonist: Id?,
    val itemType: ItemType
) {
  fun process(state: World, message: Any) {
    when (message) {
      is ItemMoved -> {
        if (message.id == id)
          position = message.position
      }
      is ColonistPickedItem -> {
        if (message.itemId == id) {
          position = null
          colonist = message.colonistId
        }
      }
      is ColonistDroppedItem -> {
        if (message.itemId == id) {
          position = state.colonists[message.colonistId]!!.position
          colonist = null
        }
      }
    }
  }
}

class Block(
    var type: BlockType = BlockAir,
    var skyLight: Int = 0,
    var blockLight: Int = 0,
    var waterLevel: Byte = 0.toByte()
)

class Zone(
    val id: Id,
    val positions: Collection<Vector3i>
)

open class World(override val dimension: Dimension): HasDimension {
  val colonists = mutableMapOf<Id, Colonist>()
  val tasks = mutableMapOf<Id, Task>()
  val blocks = ObjectMutableSpace(dimension, { Block() })
  val blockType = object : Space<BlockType> {
    override val dimension: Dimension = blocks.dimension
    override fun get(index: Int): BlockType = blocks[index].type
  }
  val blocked = object : Space<Boolean> {
    override val dimension: Dimension = blocks.dimension
    override fun get(index: Int): Boolean = blocks[index].type.opaque
  }
  val pathFinder = PathFinder(PathFinder.isNotOpaque(blocks))
  val items = mutableMapOf<Id, Item>()
  val zones = mutableMapOf<Id, Zone>()
  var timeOfDay = 0f

  // Process events
  open fun process(message: Any) {
    colonists.values.forEach { colonist -> colonist.process(this, message)}
    tasks.values.forEach { task -> task.process(this, message) }
    items.values.forEach { item -> item.process(this, message) }
    when (message) {
      is WorldGenerated -> {
        message.blocks.forEach { (blockIndex, block) ->
          val blockPos = message.offset + message.blocks.vector3i(blockIndex)
          this.blocks[blockPos].type = block.type
        }
      }
      is BlockUpdated -> message.updates.forEach { pos, value -> this.blocks[pos].type = value }
      is BlockWaterLevelUpdated -> message.updates.forEach { pos, value -> this.blocks[pos].waterLevel = value}
      is SkyLightUpdated -> message.updates.forEach { pos, value -> this.blocks[pos].skyLight = value }
      is BlockLightUpdated -> message.updates.forEach { pos, value -> this.blocks[pos].blockLight = value }
      is TaskCreated -> tasks[message.id] = Task(message.id, message.taskType, message.priority, listOf())
      is TaskCompleted -> tasks.remove(message.id)
      is TaskCancelled -> tasks.remove(message.id)
      is ItemSpawned -> items[message.id] = Item(message.id, message.position, null, message.itemType)
      is ColonistSpawned -> colonists[message.id] = Colonist(message.id, message.initialPos, Vector3i.AXIS_Z, mutableMapOf(), mutableMapOf(), mutableListOf(), null)
      is TimeUpdated -> timeOfDay = message.time
      is ZoneCreated -> zones[message.id] = Zone(message.id, message.area)
    }
  }
}