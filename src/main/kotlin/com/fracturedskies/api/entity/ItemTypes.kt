package com.fracturedskies.api.entity

import com.fracturedskies.api.block.*
import com.fracturedskies.api.entity.model.*
import com.fracturedskies.engine.math.Color4


open class ItemType {
  open val blockType: BlockType? = null
  open val model: EntityModel = NoopEntityModel
}

object ItemDirt: ItemType() {
  override val blockType = BlockTypeDirt
  override val model: EntityModel get() = ColorEntityModel(Color4(178, 161, 130, 255))
}

object ItemWood: ItemType() {
  override val blockType: BlockType? = BlockTypeWood
  override val model: EntityModel get() = ColorEntityModel(Color4.DARK_BROWN)
}

object ItemStone: ItemType() {
  override val blockType: BlockType? = BlockTypeStone
  override val model: EntityModel get() = ColorEntityModel(Color4.GRAY)
}

object ItemLight: ItemType() {
  override val blockType: BlockType? = BlockTypeLight
  override val model: EntityModel get() = ColorEntityModel(Color4.WHITE)
}
