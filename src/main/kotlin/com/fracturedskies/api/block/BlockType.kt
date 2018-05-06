package com.fracturedskies.api.block

import com.fracturedskies.api.MAX_LIGHT_LEVEL
import com.fracturedskies.api.block.data.*
import com.fracturedskies.api.entity.*
import com.fracturedskies.engine.math.Color4
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

abstract class BlockType {
  open val color: Color4 get() = Color4(255, 255, 255, 0)
  open val light: Int get() = 0
  open val opaque: Boolean get() = true
  open val itemDrop: ItemType? get() = null
  open fun supportedProperties(): List<KClass<*>> = listOf(WaterLevel::class, SkyLight::class, BlockLight::class)
  open fun <T: Any> defaultValue(property: KClass<T>): T? = property.safeCast(when (property) {
    WaterLevel::class -> WaterLevel(0.toByte())
    SkyLight::class -> SkyLight(0)
    BlockLight::class -> BlockLight(0)
    else ->
      throw IllegalArgumentException("Unsupported property ${property.simpleName} for ${javaClass.simpleName}")
  })
}

object BlockTypeAir : BlockType() {
  override val color = Color4(255, 255, 255, 0)
  override val opaque = false
}

object BlockTypeDirt: BlockType() {
  override val color = Color4(178, 161, 130, 255)
  override val itemDrop = ItemDirt
}

object BlockTypeGrass: BlockType() {
  override val color = Color4.GREEN
  override val itemDrop = ItemDirt
}

object BlockTypeWood: BlockType() {
  override val color = Color4.DARK_BROWN
  override val itemDrop = ItemWood
}

object BlockTypeLeaves: BlockType() {
  override val color = Color4.DARK_GREEN
}

object BlockTypeStone: BlockType() {
  override val color = Color4.GRAY
  override val itemDrop = ItemStone
}

object BlockTypeLight: BlockType() {
  override val color = Color4.WHITE
  override val light: Int = MAX_LIGHT_LEVEL + 1
  override val itemDrop = ItemLight
}
