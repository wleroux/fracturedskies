package com.fracturedskies.game

class World(val width: Int, val height: Int, val depth: Int, init: (Int, Int, Int) -> Block) {
  private val blocks = Array<Block>(width * height * depth) { index ->
    val x = index % width
    val y = (index - x) / width % height
    val z = (((index - x) / width) - y) / height

    init(x, y, z)
  }

  operator fun get(x: Int, y: Int, z: Int): Block {
    val index = (z * width * height) + (y * width) + x
    return blocks[index]
  }

  operator fun set(x: Int, y: Int, z: Int, value: Block) {
    if (x !in 0 until width || y !in 0 until height || z !in 0 until depth) {
      throw IllegalArgumentException("Cannot access coordinate [$x, $y, $z] in a world of size [$width, $height, $depth]")
    }
    val index = ((z * height) + y) * width + x
    blocks[index] = value
  }

  override fun toString(): String {
    return blocks.joinToString()
  }
}