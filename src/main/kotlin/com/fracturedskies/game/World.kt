package com.fracturedskies.game

import com.fracturedskies.engine.collections.Dimension
import com.fracturedskies.engine.math.Vector3i

class World(val dimension: Dimension, init: (Vector3i) -> Block) {
  private val blocks = Array(dimension.size) { index -> init(dimension(index)) }
  fun has(position: Vector3i) = has(position.x, position.y, position.z)
  fun has(x: Int, y: Int, z: Int) = dimension.has(x, y, z)
  operator fun get(position: Vector3i) = get(position.x, position.y, position.z)
  operator fun get(x: Int, y: Int, z: Int) = blocks[dimension(x, y, z)]

  operator fun set(x: Int, y: Int, z: Int, value: Block) {
    if (!has(x, y, z))
      throw IllegalArgumentException("Cannot access coordinate [$x, $y, $z] in a world of size $dimension")
    blocks[dimension(x, y, z)] = value
  }

  override fun toString(): String {
    return "WORLD"
  }
}