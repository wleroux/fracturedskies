package com.fracturedskies.game.messages

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.Message
import com.fracturedskies.game.BlockType
import com.fracturedskies.game.World
import com.fracturedskies.game.workers.Work

data class WorldGenerated(val world: World, override val cause: Cause, override val context: Context): Message
data class NewGameRequested(override val cause: Cause, override val context: Context): Message

data class QueueWork(val work: Work, override val cause: Cause, override val context: Context): Message

data class UpdateBlock(val x: Int, val y: Int, val z: Int, val type: BlockType, override val cause: Cause, override val context: Context): Message