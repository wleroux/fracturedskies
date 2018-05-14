package com.fracturedskies.api.block

import com.fracturedskies.api.MAX_LIGHT_LEVEL
import com.fracturedskies.api.block.model.*
import com.fracturedskies.api.entity.*
import com.fracturedskies.engine.math.Color4


object BlockTypeAir : BlockType() {
  override val model = NoopBlockModel
  override val opaque = false
}

object BlockTypeDirt: BlockType() {
  override val model = ColorBlockModel(Color4(178, 161, 130, 255))
  override val itemDrop = ItemDirt
}

object BlockTypeGrass: BlockType() {
  override val model = ColorBlockModel(Color4.GREEN)
  override val itemDrop = ItemDirt
}

object BlockTypeWood: BlockType() {
  override val model = ColorBlockModel(Color4.DARK_BROWN)
  override val itemDrop = ItemWood
}

object BlockTypeLeaves: BlockType() {
  override val model = ColorBlockModel(Color4.DARK_GREEN)
}

object BlockTypeStone: BlockType() {
  override val model = ColorBlockModel(Color4.GRAY)
  override val itemDrop = ItemStone
}

object BlockTypeLight: BlockType() {
  override val model = ColorBlockModel(Color4.WHITE)
  override val light: Int = MAX_LIGHT_LEVEL + 1
  override val itemDrop = ItemLight
}

class ColorBlockType(private val color: Color4): BlockType() {
  override val model: BlockModel get() = ColorBlockModel(color)
}