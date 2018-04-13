package com.fracturedskies.render.world

import com.fracturedskies.api.BlockType

data class Block(val type: BlockType, val skyLight: Int, val blockLight: Int, val waterLevel: Byte) {
  override fun toString(): String {
    return "$type"
  }
}