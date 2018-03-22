package com.fracturedskies.api

import com.fracturedskies.engine.math.Color4
import com.fracturedskies.light.api.MAX_LIGHT_LEVEL


enum class BlockType(val color: Color4, val blockLight: Int) {
  AIR(Color4.BLACK, 0),
  GRASS(Color4.GREEN, 0),
  BLOCK(Color4.BROWN, 0),
  LIGHT(Color4.WHITE, MAX_LIGHT_LEVEL + 1);

  val opaque: Boolean get() {
    return this != AIR
  }
}
