package com.fracturedskies.api

import com.fracturedskies.api.block.Block
import com.fracturedskies.api.entity.ItemType
import com.fracturedskies.api.task.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.api.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i

enum class GameSpeed(val msBetweenUpdates: Long) { PAUSE(Long.MAX_VALUE), SLOW(450L), NORMAL(150L), FAST(50L), UNLIMITED(0L) }

data class NewGameRequested(val dimension: Dimension, val seed: Int,
                            override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
data class GameSpeedUpdated(val gameSpeed: GameSpeed,
                            override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message

data class WorldGenerated(val blocks: ObjectSpace<Block>,
                          override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
data class BlockUpdate(val position: Vector3i, val original: Block, val target: Block)
data class BlocksUpdated(val blocks: List<BlockUpdate>,
                         override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
data class TimeUpdated(val time: Float,
                       override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message

data class ColonistSpawned(val colonistId: Id, val position: Vector3i,
                           override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
data class ColonistMoved(val colonistId: Id, val position: Vector3i, val direction: Vector3i,
                         override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
data class ColonistPickedItem(val colonistId: Id, val itemId: Id,
                              override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
data class ColonistDroppedItem(val colonistId: Id, val itemId: Id,
                               override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message

data class ColonistTaskSelected(val colonistId: Id, val taskId: Id?,
                                override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
data class ColonistRejectedTask(val colonistId: Id, val taskId: Id,
                                override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message

data class TaskCreated(val taskId: Id, val taskType: TaskType, val taskPriority: TaskPriority,
                       override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
data class TaskCancelled(val colonistId: Id, val taskId: Id,
                         override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
data class TaskCompleted(val colonistId: Id, val taskId: Id,
                         override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message

data class ItemMoved(val itemId: Id, val position: Vector3i,
                     override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
data class ItemSpawned(val itemId: Id, val itemType: ItemType, val position: Vector3i,
                       override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message

data class ZoneCreated(val zoneId: Id, val positions: List<Vector3i>,
                       override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
