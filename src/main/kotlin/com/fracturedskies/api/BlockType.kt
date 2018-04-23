package com.fracturedskies.api

import com.fracturedskies.engine.math.Color4
import com.fracturedskies.light.api.MAX_LIGHT_LEVEL


enum class BlockType(val color: Color4, val blockLight: Int) {
  AIR(Color4.BLACK, 0),
  DIRT(Color4.BROWN, 0) {
    override val blockDrop get() = DIRT
  },
  GRASS(Color4.GREEN, 0)  {
    override val blockDrop get() = DIRT
  },
  WOOD(Color4.DARK_BROWN, 0) {
    override val blockDrop get() = WOOD
  },
  LEAVE(Color4.DARK_GREEN, 0),
  STONE(Color4.GRAY, 0) {
    override val blockDrop get() = STONE
  },
  LIGHT(Color4.WHITE, MAX_LIGHT_LEVEL + 1) {
    override val blockDrop get() = LIGHT
  };

  open val blockDrop: BlockType? get() = null

  val opaque: Boolean get() {
    return this != AIR
  }
}
