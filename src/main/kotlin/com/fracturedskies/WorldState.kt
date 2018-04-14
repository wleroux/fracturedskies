package com.fracturedskies

import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.task.api.*
import com.fracturedskies.task.api.TaskPriority.AVERAGE
import com.fracturedskies.task.behavior.*
import java.util.function.Predicate
import kotlin.LazyThreadSafetyMode.PUBLICATION


class Colonist(
    val id: Id,
    var position: Vector3i,
    val categoryPriorities: MutableMap<TaskCategory, TaskPriority>,
    val rejectedTasks: MutableMap<Id, Int>,
    var assignedTask: Id?
) {
  override fun hashCode() = id.hashCode()
  override fun equals(other: Any?) = when (other) {
    is Colonist -> id == other.id
    else -> false
  }

  fun process(message: Any) {
    when (message) {
      is ColonistMoved -> if (message.id == id) { position = message.pos }
      is ColonistTaskSelected -> if (message.colonist == id) { assignedTask = message.task }
      is ColonistRejectedTask -> {
        if (message.task == assignedTask) assignedTask = null
        if (message.colonist == id) rejectedTasks[message.task] = (rejectedTasks[message.task] ?: 0) + 1
      }
      is TaskCompleted -> if (assignedTask == message.id) { assignedTask = null }
      is TaskCancelled -> if (assignedTask == message.id) { assignedTask = null }
    }
  }
}

class Task<out T>(
    val id: Id,
    val category: TaskCategory,
    val condition: Condition,
    val details: T,
    var priority: TaskPriority = AVERAGE,
    var assigned: List<Id>
) {
  val behavior: Behavior by lazy(PUBLICATION) {
    return@lazy when (details) {
      is PlaceBlock -> placeBlock(details.pos, details.blockType)
      is RemoveBlock -> removeBlock(details.pos)
      else -> NoopBehavior
    }
  }

  override fun hashCode() = id.hashCode()
  override fun equals(other: Any?) = when(other) {
    is Task<*> -> id == other.id
    else -> false
  }

  fun process(message: Any) {
    when (message) {
      is ColonistRejectedTask -> {
        if (message.task == id)
          assigned -= message.colonist
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
    var position: Vector3i,
    val blockType: BlockType
) {
  fun process(message: Any) {
    when (message) {
      is ItemMoved -> {
        if (message.id == id)
          position = message.position
      }
    }
  }
}

class Block(
    var type: BlockType = AIR,
    var skyLight: Int = 0,
    var blockLight: Int = 0,
    var waterLevel: Byte = 0.toByte()
)

open class WorldState(val dimension: Dimension) {
  val colonists = mutableMapOf<Id, Colonist>()
  val tasks = mutableMapOf<Id, Task<*>>()
  val blocks = ObjectMutableSpace(dimension, { Block() })
  val blockType = object : Space<BlockType> {
    override val dimension: Dimension = blocks.dimension
    override fun get(index: Int): BlockType = blocks[index].type
  }
  val blocked = object : Space<Boolean> {
    override val dimension: Dimension = blocks.dimension
    override fun get(index: Int): Boolean = blocks[index].type.opaque
  }
  val pathFinder = PathFinder(Predicate { pos -> !(blocked.has(pos) && !blocked[pos])})
  val items = mutableMapOf<Id, Item>()
  var timeOfDay = 0f

  open fun process(message: Any) {
    colonists.values.forEach { colonist -> colonist.process(message)}
    tasks.values.forEach { task -> task.process(message) }
    items.values.forEach { item -> item.process(message) }
    when (message) {
      is BlocksGenerated -> { message.blocks.forEach { (blockIndex, blockType) ->
        val blockPos = message.offset + message.blocks.dimension.toVector3i(blockIndex)
        this.blocks[blockPos].type = blockType
      } }
      is BlockUpdated -> message.updates.forEach { pos, value -> this.blocks[pos].type = value }
      is BlockWaterLevelUpdated -> message.updates.forEach { pos, value -> this.blocks[pos].waterLevel = value}
      is SkyLightUpdated -> message.updates.forEach { pos, value -> this.blocks[pos].skyLight = value }
      is BlockLightUpdated -> message.updates.forEach { pos, value -> this.blocks[pos].blockLight = value }
      is TaskCreated<*> -> tasks[message.id] = Task(message.id, message.category, message.condition, message.details, message.priority, listOf())
      is TaskCompleted -> tasks.remove(message.id)
      is TaskCancelled -> tasks.remove(message.id)
      is ItemSpawned -> items[message.id] = Item(message.id, message.position, message.blockType)
      is ColonistSpawned -> colonists[message.id] = Colonist(message.id, message.initialPos, mutableMapOf(), mutableMapOf(), null)
      is TimeUpdated -> timeOfDay = message.time
    }
  }
}