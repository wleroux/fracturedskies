package com.fracturedskies.game

class Block(var type: BlockType, var skyLight: Int) {
  override fun toString(): String {
    return "$type"
  }
}