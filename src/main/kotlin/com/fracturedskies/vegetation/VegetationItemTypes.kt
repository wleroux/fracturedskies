package com.fracturedskies.vegetation

import com.fracturedskies.api.block.BlockType
import com.fracturedskies.api.entity.ItemType
import com.fracturedskies.engine.math.Color4


object ItemTypeTomato: ItemType() {
  override val blockType: BlockType? = BlockTypeTomato1
  override val color: Color4 = BlockTypeTomato5.color
}