package com.fracturedskies.game.workers

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus
import com.fracturedskies.game.BlockType
import com.fracturedskies.game.messages.UpdateBlock
import kotlinx.coroutines.experimental.runBlocking

class UpdateBlockWork(val x: Int, val y: Int, val z: Int, val blockType: BlockType, type: WorkType, priority: Int) : Work(type, priority) {
  override fun invoke() {
    runBlocking {
      MessageBus.dispatch(UpdateBlock(x, y, z, blockType, Cause.of(this), Context()))
    }
  }
}