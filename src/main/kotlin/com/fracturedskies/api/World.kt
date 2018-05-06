package com.fracturedskies.api

import com.fracturedskies.api.GameSpeed.NORMAL
import com.fracturedskies.api.block.*
import com.fracturedskies.api.entity.*
import com.fracturedskies.api.task.*
import com.fracturedskies.api.zone.Zone
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.api.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import javax.enterprise.event.Event
import javax.inject.*

@Singleton
class World: HasDimension {
  var started: Boolean = false

  override var dimension: Dimension = GameSize.MINI.dimension
  var blocks = ObjectMutableSpace(dimension, { Block(BlockTypeAir) })
  var pathFinder = PathFinder(PathFinder.isNotOpaque(blocks))
  var gameSpeed: GameSpeed = NORMAL

  var timeOfDay = 0f

  val colonists = mutableMapOf<Id, Colonist>()
  val tasks = mutableMapOf<Id, Task>()
  val items = mutableMapOf<Id, Item>()
  val zones = mutableMapOf<Id, Zone>()

  @Inject
  private lateinit var events: Event<Message>

  // Colonist Actions

  fun spawnColonist(id: Id, position: Vector3i, cause: Cause) {
    colonists[id] = Colonist(id, position, Vector3i.AXIS_Z, mutableMapOf(), mutableMapOf(), mutableListOf(), null)
    events.fire(ColonistSpawned(id, position, cause))
  }

  fun moveColonist(id: Id, pos: Vector3i, direction: Vector3i, cause: Cause) {
    colonists[id]!!.position = pos
    colonists[id]!!.direction = direction
    events.fire(ColonistMoved(id, pos, direction, cause))
  }

  // Task Actions

  fun createTask(id: Id, taskType: TaskType, priority: TaskPriority, cause: Cause) {
    tasks[id] = Task(id, taskType, priority, listOf())
    events.fire(TaskCreated(id, taskType, priority, cause))
  }

  // Block Actions

  fun updateBlock(position: Vector3i, block: Block, cause: Cause) {
    updateBlocks(mapOf(position to block), cause)
  }

  fun updateBlocks(blocks: Map<Vector3i, Block>, cause: Cause) {
    val updates = blocks.map { (position, block) ->
      BlockUpdate(position, this.blocks[position], block)
    }.toList()

    updates.forEach { update ->
      this.blocks[update.position] = update.target
    }
    events.fire(BlocksUpdated(updates, cause))
  }

  // Item Actions

  fun spawnItem(id: Id, itemType: ItemType, position: Vector3i, cause: Cause) {
    items[id] = Item(id, position, null, itemType)
    events.fire(ItemSpawned(id, itemType, position, cause))
  }

  fun moveItem(itemId: Id, position: Vector3i, cause: Cause) {
    val item = items[itemId]!!
    if (item.colonist != null)
      throw IllegalStateException("Cannot move item '$itemId'; currently in possession by colonist.")

    item.position = position
    events.fire(ItemMoved(itemId, position, cause))
  }

  // Zone Actions

  fun createZone(id: Id, positions: List<Vector3i>, cause: Cause) {
    zones[id] = Zone(id, positions)
    events.fire(ZoneCreated(id, positions, cause))
  }

  // Colonist-Task Actions

  fun selectTask(colonistId: Id, taskId: Id?, cause: Cause) {
    val colonist = colonists[colonistId]!!
    val previousTask = colonist.assignedTask
    if (previousTask != null) {
      tasks[previousTask]!!.assigned -= colonistId
    }
    colonists[colonistId]!!.assignedTask = taskId
    if (taskId != null) {
      tasks[taskId]!!.assigned += colonistId
    }
    events.fire(ColonistTaskSelected(colonistId, taskId, cause))
  }

  fun rejectTask(colonistId: Id, taskId: Id, cause: Cause) {
    val colonist = colonists[colonistId]!!
    if (colonist.assignedTask != taskId)
      throw IllegalStateException("Cannot reject task '$taskId'; Colonist '$colonistId' not assigned.")

    colonist.assignedTask = null
    colonist.rejectedTasks[taskId] = (colonist.rejectedTasks[taskId] ?: 0) + 1
    tasks[taskId]!!.assigned -= colonistId
    events.fire(ColonistRejectedTask(colonistId, taskId, cause))
  }

  fun cancelTask(colonistId: Id, taskId: Id, cause: Cause) {
    val colonist = colonists[colonistId]!!
    if (colonist.assignedTask != taskId)
      throw IllegalStateException("Cannot cancel task '$taskId'; Colonist '$colonistId' not assigned.")
    colonist.assignedTask = null
    tasks[taskId]!!.assigned -= colonistId
    tasks.remove(taskId)
    events.fire(TaskCancelled(colonistId, taskId, cause))
  }

  fun completeTask(colonistId: Id, taskId: Id, cause: Cause) {
    val colonist = colonists[colonistId]!!
    if (colonist.assignedTask != taskId)
      throw IllegalStateException("Cannot cancel task '$taskId'; Colonist '$colonistId' not assigned.")
    colonist.assignedTask = null
    tasks[taskId]!!.assigned -= colonistId
    tasks.remove(taskId)
    events.fire(TaskCompleted(colonistId, taskId, cause))
  }

  // Colonist-Items Action
  fun pickItem(colonistId: Id, itemId: Id, cause: Cause) {
    val prevColonistId = items[itemId]!!.colonist
    if (prevColonistId != null) {
      colonists[prevColonistId]!!.inventory -= itemId
    }

    colonists[colonistId]!!.inventory += itemId
    items[itemId]!!.colonist = colonistId
    items[itemId]!!.position = null
    events.fire(ColonistPickedItem(colonistId, itemId, cause))
  }

  fun dropItem(colonistId: Id, itemId: Id, cause: Cause) {
    val colonist = colonists[colonistId]!!
    if (!colonist.inventory.contains(itemId))
      throw IllegalArgumentException("Cannot drop item '$itemId': colonist '$colonistId' does not have it.")

    colonist.inventory -= itemId
    items[itemId]!!.colonist = null
    items[itemId]!!.position = colonist.position
    events.fire(ColonistDroppedItem(colonistId, itemId, cause))
  }

  // Game Controls

  fun updateSpeed(gameSpeed: GameSpeed, cause: Cause) {
    this.gameSpeed = gameSpeed
    events.fire(GameSpeedUpdated(gameSpeed, cause))
  }

  fun requestShutdown(cause: Cause) {
    events.fire(ShutdownRequested(cause))
  }

  fun startGame(gameSize: GameSize, cause: Cause) {
    if (started)
      throw IllegalStateException("Game already started.")

    started = true
    dimension = gameSize.dimension
    blocks = ObjectMutableSpace(dimension, { Block(BlockTypeAir) })
    pathFinder = PathFinder(PathFinder.isNotOpaque(blocks))

    events.fire(NewGameRequested(gameSize.dimension, 0, cause))
  }

  fun updateTimeOfDay(timeOfDay: Float, cause: Cause) {
    this.timeOfDay = timeOfDay
    events.fire(TimeUpdated(timeOfDay, cause))
  }

  fun generateWorld(blocks: ObjectMutableSpace<Block>, cause: Cause) {
    blocks.forEach { blockPos, block ->
      this.blocks[blockPos] = block
    }
    events.fire(WorldGenerated(blocks, cause))
  }
}