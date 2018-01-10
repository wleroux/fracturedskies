package com.fracturedskies.game.messages

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.*
import com.fracturedskies.game.*

data class WorldGenerated(val world: ObjectMap<Block>, override val cause: Cause, override val context: Context): Message
data class NewGameRequested(override val cause: Cause, override val context: Context): Message

data class UpdateBlock(val updates: Map<Vector3i, BlockType>, override val cause: Cause, override val context: Context): Message
data class UpdateBlockWater(val updates: Map<Vector3i, Byte>, override val cause: Cause, override val context: Context): Message
data class SkyLightUpdated(val updates: Map<Vector3i, Int>, override val cause: Cause, override val context: Context): Message
data class BlockLightUpdated(val updates: Map<Vector3i, Int>, override val cause: Cause, override val context: Context): Message
data class TimeUpdated(val time: Float, override val cause: Cause, override val context: Context): Message

data class AddWorker(val pos: Vector3i, override val cause: Cause, override val context: Context): Message