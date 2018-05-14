package com.fracturedskies.vegetation

import com.fracturedskies.api.block.*
import com.fracturedskies.api.block.model.*
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

data class Growth(val probability: Float, val block: () -> Block, val isProperSoil: (Block) -> Boolean)

object BlockTypeTomato1 : BlockType() {
  override val opaque: Boolean get() = false
  override val model: BlockModel = FileBlockModel("tomato_1.mesh")
  override fun supportedProperties() = super.supportedProperties() + Growth::class
  override fun <T : Any> defaultValue(property: KClass<T>): T? = property.safeCast(when(property) {
    Growth::class -> Growth(0.1f, {Block(BlockTypeTomato2)}, { block -> setOf(BlockTypeDirt, BlockTypeGrass).contains(block.type) })
    else -> super.defaultValue(property)
  })
}

object BlockTypeTomato2 : BlockType() {
  override val opaque: Boolean get() = false
  override val model: BlockModel = FileBlockModel("tomato_2.mesh")
  override fun supportedProperties() = super.supportedProperties() + Growth::class
  override fun <T : Any> defaultValue(property: KClass<T>): T? = property.safeCast(when(property) {
    Growth::class -> Growth(0.1f, {Block(BlockTypeTomato3)}, { block -> setOf(BlockTypeDirt, BlockTypeGrass).contains(block.type) })
    else -> super.defaultValue(property)
  })
}
object BlockTypeTomato3 : BlockType() {
  override val opaque: Boolean get() = false
  override val model: BlockModel = FileBlockModel("tomato_3.mesh")
  override fun supportedProperties() = super.supportedProperties() + Growth::class
  override fun <T : Any> defaultValue(property: KClass<T>): T? = property.safeCast(when(property) {
    Growth::class -> Growth(0.1f, {Block(BlockTypeTomato4)}, { block -> setOf(BlockTypeDirt, BlockTypeGrass).contains(block.type) })
    else -> super.defaultValue(property)
  })
}
object BlockTypeTomato4 : BlockType() {
  override val opaque: Boolean get() = false
  override val model: BlockModel = FileBlockModel("tomato_4.mesh")
  override fun supportedProperties() = super.supportedProperties() + Growth::class
  override fun <T : Any> defaultValue(property: KClass<T>): T? = property.safeCast(when(property) {
    Growth::class -> Growth(0.1f, {Block(BlockTypeTomato5)}, { block -> setOf(BlockTypeDirt, BlockTypeGrass).contains(block.type) })
    else -> super.defaultValue(property)
  })
}
object BlockTypeTomato5 : BlockType() {
  override val opaque: Boolean get() = false
  override val model: BlockModel = FileBlockModel("tomato_5.mesh")
  override val itemDrop = ItemTypeTomato
  override fun supportedProperties() = super.supportedProperties() + Growth::class
  override fun <T : Any> defaultValue(property: KClass<T>): T? = property.safeCast(when(property) {
    Growth::class -> Growth(0f, {Block(BlockTypeTomato5)}, { block -> setOf(BlockTypeDirt, BlockTypeGrass).contains(block.type) })
    else -> super.defaultValue(property)
  })
}
