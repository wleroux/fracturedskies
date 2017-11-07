package com.fracturedskies.game


enum class BlockType(val top: Int, val front: Int, val left: Int, val back: Int, val right: Int, val bottom: Int) {
  AIR(-1, -1),
  BLOCK(2, 1);

  constructor(top: Int, sides: Int) : this(top, sides, sides, sides, sides, sides)
}
