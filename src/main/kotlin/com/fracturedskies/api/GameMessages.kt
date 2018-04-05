package com.fracturedskies.api

import com.fracturedskies.colonist.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.*

data class NewGameRequested(val dimension: Dimension, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class BlockUpdated(val updates: Map<Vector3i, BlockType>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class BlockWaterLevelUpdated(val updates: Map<Vector3i, Byte>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class SkyLightUpdated(val updates: Map<Vector3i, Int>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class BlockLightUpdated(val updates: Map<Vector3i, Int>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class TimeUpdated(val time: Float, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class ColonistSpawned(val id: Id, val initialPos: Vector3i, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class ColonistTaskSelected(val colonist: Id, val task: Id?, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class ColonistMoved(val movements: Map<Id, Vector3i>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class TaskCreated<out T>(val id: Id, val category: Category, val priority: Priority, val condition: Condition, val taskDetails: T, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class TaskCompleted(val id: Id, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message