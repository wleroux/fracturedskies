package com.fracturedskies.api

import com.fracturedskies.Block
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.*
import com.fracturedskies.task.api.TaskPriority

data class NewGameRequested(val dimension: Dimension, val seed: Int, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
enum class GameSpeed(val msBetweenUpdates: Long) { PAUSE(Long.MAX_VALUE), SLOW(450L), NORMAL(150L), FAST(50L), UNLIMITED(0L) }
data class GameSpeedUpdated(val gameSpeed: GameSpeed, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class WorldGenerated(val offset: Vector3i, val blocks: ObjectSpace<Block>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class BlockUpdated(val updates: Map<Vector3i, BlockType>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class BlockWaterLevelUpdated(val updates: Map<Vector3i, Byte>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class SkyLightUpdated(val updates: Map<Vector3i, Int>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class BlockLightUpdated(val updates: Map<Vector3i, Int>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class TimeUpdated(val time: Float, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class ColonistSpawned(val id: Id, val initialPos: Vector3i, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class ColonistMoved(val id: Id, val pos: Vector3i, val direction: Vector3i, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class ColonistPickedItem(val colonistId: Id, val itemId: Id, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class ColonistDroppedItem(val colonistId: Id, val itemId: Id, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class ColonistTaskSelected(val colonist: Id, val task: Id?, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class ColonistRejectedTask(val colonistId: Id, val taskId: Id, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class TaskCreated(val id: Id, val taskType: TaskType, val priority: TaskPriority, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class TaskCancelled(val id: Id, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class TaskCompleted(val id: Id, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class ItemMoved(val id: Id, val position: Vector3i, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class ItemSpawned(val id: Id, val itemType: ItemType, val position: Vector3i, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class ZoneCreated(val id: Id, val area: Collection<Vector3i>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
