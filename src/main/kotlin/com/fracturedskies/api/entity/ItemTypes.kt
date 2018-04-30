package com.fracturedskies.api.entity

import com.fracturedskies.api.block.*
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
  override val blockType: BlockType? = BlockTypeLight
}
