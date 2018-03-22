package com.fracturedskies.render.components.world

import com.fracturedskies.api.BlockType

class Block(var type: BlockType, var skyLight: Int, var blockLight: Int, var waterLevel: Byte) {
  override fun toString(): String {
    return "$type"
  }
}