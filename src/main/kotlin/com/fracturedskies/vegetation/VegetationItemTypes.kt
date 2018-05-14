package com.fracturedskies.vegetation

import com.fracturedskies.api.block.BlockType
import com.fracturedskies.api.entity.ItemType
import com.fracturedskies.api.entity.model.*
import com.fracturedskies.engine.math.Color4


object ItemTypeTomato: ItemType() {
  override val blockType: BlockType? = BlockTypeTomato1
  override val model: EntityModel get() = ColorEntityModel(Color4(255, 12, 12, 255))
}