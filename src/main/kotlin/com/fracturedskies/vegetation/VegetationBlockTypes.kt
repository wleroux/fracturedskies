package com.fracturedskies.vegetation

import com.fracturedskies.api.block.*
import com.fracturedskies.engine.math.Color4
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

data class Growth(val probability: Float, val block: () -> Block, val isProperSoil: (Block) -> Boolean)

object BlockTypeTomato1 : BlockType() {
  override val opaque: Boolean = true
  override val color: Color4 = Color4(65, 76, 39, 255)
  override fun supportedProperties() = super.supportedProperties() + Growth::class
  override fun <T : Any> defaultValue(property: KClass<T>): T? = property.safeCast(when(property) {
    Growth::class -> Growth(0.1f, {Block(BlockTypeTomato2)}, { block -> setOf(BlockTypeDirt, BlockTypeGrass).contains(block.type) })
    else -> super.defaultValue(property)
  })
}

object BlockTypeTomato2 : BlockType() {
  override val opaque: Boolean = true
  override val color: Color4 = Color4(102, 108, 63, 255)
  override fun supportedProperties() = super.supportedProperties() + Growth::class
  override fun <T : Any> defaultValue(property: KClass<T>): T? = property.safeCast(when(property) {
    Growth::class -> Growth(0.1f, {Block(BlockTypeTomato3)}, { block -> setOf(BlockTypeDirt, BlockTypeGrass).contains(block.type) })
    else -> super.defaultValue(property)
  })
}
object BlockTypeTomato3 : BlockType() {
  override val opaque: Boolean = true
  override val color: Color4 = Color4(162, 134, 74, 255)
  override fun supportedProperties() = super.supportedProperties() + Growth::class
  override fun <T : Any> defaultValue(property: KClass<T>): T? = property.safeCast(when(property) {
    Growth::class -> Growth(0.1f, {Block(BlockTypeTomato4)}, { block -> setOf(BlockTypeDirt, BlockTypeGrass).contains(block.type) })
    else -> super.defaultValue(property)
  })
}
object BlockTypeTomato4 : BlockType() {
  override val opaque: Boolean = true
  override val color: Color4 = Color4(179, 83, 64, 255)
  override fun supportedProperties() = super.supportedProperties() + Growth::class
  override fun <T : Any> defaultValue(property: KClass<T>): T? = property.safeCast(when(property) {
    Growth::class -> Growth(0.1f, {Block(BlockTypeTomato5)}, { block -> setOf(BlockTypeDirt, BlockTypeGrass).contains(block.type) })
    else -> super.defaultValue(property)
  })
}
object BlockTypeTomato5 : BlockType() {
  override val opaque: Boolean = true
  override val color: Color4 = Color4(223, 67, 55, 255)
  override val itemDrop = ItemTypeTomato
  override fun supportedProperties() = super.supportedProperties() + Growth::class
  override fun <T : Any> defaultValue(property: KClass<T>): T? = property.safeCast(when(property) {
    Growth::class -> Growth(0f, {Block(BlockTypeTomato5)}, { block -> setOf(BlockTypeDirt, BlockTypeGrass).contains(block.type) })
    else -> super.defaultValue(property)
  })
}
