package com.fracturedskies.game

class Block(var type: BlockType, var skyLight: Int, var waterLevel: Byte) {
  override fun toString(): String {
    return "$type"
  }
}