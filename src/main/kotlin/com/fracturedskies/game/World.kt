package com.fracturedskies.game

import com.fracturedskies.engine.math.Vector3i

class World(val width: Int, val height: Int, val depth: Int, init: (Int, Int, Int) -> Block) {
  private val blocks = Array(width * height * depth) { index ->
    val x = index % width
    val y = (index - x) / width % height
    val z = (((index - x) / width) - y) / height

    init(x, y, z)
  }

  fun has(position: Vector3i) = has(position.x, position.y, position.z)
  fun has(x: Int, y: Int, z: Int) = x in 0 until width && y in 0 until height && z in 0 until depth
  operator fun get(position: Vector3i) = get(position.x, position.y, position.z)
  operator fun get(x: Int, y: Int, z: Int): Block {
    val index = (z * width * height) + (y * width) + x
    return blocks[index]
  }

  operator fun set(x: Int, y: Int, z: Int, value: Block) {
    if (!has(x, y, z)) {
      throw IllegalArgumentException("Cannot access coordinate [$x, $y, $z] in a world of size [$width, $height, $depth]")
    }
    val index = ((z * height) + y) * width + x
    blocks[index] = value
  }

  override fun toString(): String {
    return "WORLD"
  }
}