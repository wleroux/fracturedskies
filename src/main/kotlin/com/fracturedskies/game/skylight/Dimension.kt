package com.fracturedskies.game.skylight

import com.fracturedskies.engine.math.Vector3i

class Dimension(val width: Int, val height: Int, val depth: Int) {
  val size = width * height * depth
  operator fun invoke(index: Int): Vector3i {
    val x = index % width
    val y = (index - x) / width % height
    val z = (((index - x) / width) - y) / height
    return Vector3i(x, y, z)
  }
  operator fun invoke(pos: Vector3i) = invoke(pos.x, pos.y, pos.z)
  operator fun invoke(x: Int, y: Int, z: Int) =
          (z * width * height) + (y * width) + x

  fun has(pos: Vector3i) = pos.x in (0 until width) && pos.y in (0 until height) && pos.z in (0 until depth)
}