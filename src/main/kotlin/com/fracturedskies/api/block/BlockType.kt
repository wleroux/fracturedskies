package com.fracturedskies.api.block

import com.fracturedskies.api.block.data.*
import com.fracturedskies.api.block.model.*
import com.fracturedskies.api.entity.ItemType
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

abstract class BlockType {
  open val model: BlockModel get() = NoopBlockModel
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
