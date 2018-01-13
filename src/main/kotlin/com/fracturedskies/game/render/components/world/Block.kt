package com.fracturedskies.game.render.components.world

import com.fracturedskies.game.BlockType

class Block(var type: BlockType, var skyLight: Int, var blockLight: Int, var waterLevel: Byte) {
  override fun toString(): String {
    return "$type"
  }
}