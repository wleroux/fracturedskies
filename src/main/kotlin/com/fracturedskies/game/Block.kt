package com.fracturedskies.game

class Block(var type: BlockType) {
  override fun toString(): String {
    return "$type"
  }
}