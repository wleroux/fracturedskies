package com.fracturedskies.game.messages

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.Message
import com.fracturedskies.game.BlockType
import com.fracturedskies.game.World
import com.fracturedskies.game.workers.Worker

data class WorldGenerated(val world: World, override val cause: Cause, override val context: Context): Message
data class NewGameRequested(override val cause: Cause, override val context: Context): Message

data class QueueWork(val work: Work, override val cause: Cause, override val context: Context): Message
data class WorkAssignedToWorker(val worker: Worker, val work: Work, override val cause: Cause, override val context: Context): Message

data class UpdateBlock(val updates: Map<Vector3i, BlockType>, override val cause: Cause, override val context: Context): Message
data class UpdateBlockWater(val updates: Map<Vector3i, Byte>, override val cause: Cause, override val context: Context): Message
data class LightUpdated(val updates: Map<Vector3i, Int>, override val cause: Cause, override val context: Context): Message
data class TimeUpdated(val time: Float, override val cause: Cause, override val context: Context): Message