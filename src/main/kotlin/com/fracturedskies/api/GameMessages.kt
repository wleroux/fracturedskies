package com.fracturedskies.api

import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.*
import com.fracturedskies.BlockType

data class NewGameRequested(val dimension: Dimension, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class UpdateBlock(val updates: Map<Vector3i, BlockType>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class UpdateBlockWater(val updates: Map<Vector3i, Byte>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class SkyLightUpdated(val updates: Map<Vector3i, Int>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class BlockLightUpdated(val updates: Map<Vector3i, Int>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class TimeUpdated(val time: Float, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class SpawnWorker(val id: Id, val initialPos: Vector3i, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class MoveWorkers(val movements: Map<Id, Vector3i>, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message