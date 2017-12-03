package com.fracturedskies.game

import com.fracturedskies.engine.math.Color4


enum class BlockType(val color: Color4) {
  AIR(Color4.BLACK),
  GRASS(Color4.GREEN),
  BLOCK(Color4.BROWN);
}
