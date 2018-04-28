package com.fracturedskies.api

import com.fracturedskies.engine.math.Color4

open class ItemType {
  open val blockType: BlockType? = null
  open val color: Color4
    get() = blockType?.color ?: Color4.WHITE
}

object ItemDirt: ItemType() {
  override val blockType = BlockDirt
}

object ItemWood: ItemType() {
  override val blockType: BlockType? = BlockWood
}

object ItemStone: ItemType() {
  override val blockType: BlockType? = BlockStone
}

object ItemLight: ItemType() {
  override val blockType: BlockType? = BlockLight
}


open class BlockType {
  open val color: Color4 = Color4(255, 255, 255, 0)
  open val light: Int = 0
  open val opaque: Boolean = true
  open val itemDrop: ItemType? = null
}

object BlockAir : BlockType() {
  override val color = Color4(255, 255, 255, 0)
  override val opaque = false
}

object BlockDirt: BlockType() {
  override val color = Color4(178, 161, 130, 255)
  override val itemDrop = ItemDirt
}

object BlockGrass: BlockType() {
  override val color = Color4.GREEN
  override val itemDrop = ItemDirt
}

object BlockWood: BlockType() {
  override val color = Color4.DARK_BROWN
  override val itemDrop = ItemWood
}

object BlockLeaves: BlockType() {
  override val color = Color4.DARK_GREEN
}

object BlockStone: BlockType() {
  override val color = Color4.GRAY
  override val itemDrop = ItemStone
}

object BlockLight: BlockType() {
  override val color = Color4.WHITE
  override val itemDrop = ItemLight
}
