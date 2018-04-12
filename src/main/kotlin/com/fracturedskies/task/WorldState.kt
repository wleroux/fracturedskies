package com.fracturedskies.task

import com.fracturedskies.api.*
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
      is ColonistMoved -> {
        if (message.id == id) {
          position = message.pos
        }
      }
      is ColonistTaskSelected -> {
        if (message.colonist == id) {
          assignedTask = message.task
        }
      }
      is ColonistRejectedTask -> {
        if (message.task == assignedTask)
          assignedTask = null
        if (message.colonist == id)
          rejectedTasks[message.task] = (rejectedTasks[message.task] ?: 0) + 1
      }
      is TaskCompleted -> {
        if (assignedTask == message.id)
          assignedTask = null
      }
      is TaskCancelled -> {
        if (assignedTask == message.id)
          assignedTask = null
      }
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
    val blockType: BlockType,
    var position: Vector3i
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

abstract class WorldState(val dimension: Dimension) {
  val colonists = mutableMapOf<Id, Colonist>()
  val tasks = mutableMapOf<Id, Task<*>>()
  val blockType = ObjectMutableSpace(dimension, {BlockType.AIR})
  val blocked = blockType.map { it.opaque }
  val pathFinder = PathFinder(Predicate { pos -> !(blocked.has(pos) && !blocked[pos])})
  val items = mutableMapOf<Id, Item>()

  open fun process(message: Any) {
    colonists.values.forEach { colonist -> colonist.process(message)}
    tasks.values.forEach { task -> task.process(message) }
    items.values.forEach { item -> item.process(message) }
    when (message) {
      is BlockUpdated -> message.updates.forEach { pos, blockType -> this.blockType[pos] = blockType }
      is TaskCreated<*> -> tasks[message.id] = Task(message.id, message.category, message.condition, message.details, message.priority, listOf())
      is TaskCompleted -> tasks.remove(message.id)
      is TaskCancelled -> tasks.remove(message.id)
      is ItemSpawned -> items[message.id] = Item(message.id, message.blockType, message.position)
      is ColonistSpawned -> colonists[message.id] = Colonist(message.id, message.initialPos, mutableMapOf(), mutableMapOf(), null)
    }
  }
}