package com.fracturedskies.game.messages

import com.fracturedskies.game.BlockType

data class UpdateBlockWork(val x: Int, val y: Int, val z: Int, val blockType: BlockType, override val type: WorkType, override val priority: Int) : Work