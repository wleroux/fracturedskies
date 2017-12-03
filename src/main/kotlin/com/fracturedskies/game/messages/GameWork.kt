package com.fracturedskies.game.messages

import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.game.BlockType

data class UpdateBlockWork(val pos: Vector3i, val blockType: BlockType, override val type: WorkType, override val priority: Int) : Work
data class UpdateBlockWaterWork(val pos: Vector3i, val waterLevel: Byte, override val type: WorkType, override val priority: Int) : Work