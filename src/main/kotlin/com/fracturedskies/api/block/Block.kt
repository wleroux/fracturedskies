package com.fracturedskies.api.block

import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

class Block {
  val type: BlockType
  private val data: Map<KClass<*>, Any?>
  constructor(type: BlockType) {
    this.type = type
    this.data = type.supportedProperties().map { property ->
      property to type.defaultValue(property)
    }.toMap()
  }
  private constructor(type: BlockType, data: Map<KClass<*>, Any?>) {
    this.type = type
    this.data = data
  }

  fun <T: Any> has(property: KClass<T>): Boolean {
    return get(property) != null
  }
  operator fun <T: Any> get(property: KClass<T>): T? {
    return property.safeCast(this.data[property])
  }

  fun <T: Any> with(value: T): Block {
    if (!type.supportedProperties().contains(value::class))
      throw IllegalArgumentException("Block Type '$type' does not support property '${value::class.simpleName}'.")
    if (value == get(value::class)) return this
    return Block(type, data.toMutableMap().apply {
      set(value::class, value)
    })
  }
  fun <T: Any> without(property: KClass<T>): Block {
    val defaultValue = type.defaultValue(property)
    if (defaultValue == get(property)) return this
    if (!has(property)) return this
    return Block(type, data.toMutableMap().apply {
      remove(property)
    })
  }

  override fun toString(): String {
    return type.toString()
  }
}
