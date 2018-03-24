package com.fracturedskies.engine.collections

import com.fracturedskies.engine.math.Vector3i

class Dimension(val width: Int, val height: Int, val depth: Int) {
  val size = width * height * depth

  fun has(index: Int) = index in (0 until size)
  fun has(pos: Vector3i) = has(pos.x, pos.y, pos.z)
  fun has(x: Int, y: Int, z: Int) = x in (0 until width) && y in (0 until height) && z in (0 until depth)

  operator fun invoke(pos: Vector3i) = invoke(pos.x, pos.y, pos.z)
  operator fun invoke(x: Int, y: Int, z: Int) = (y * width * depth) + (z * width) + x

  inline fun forEach(block: (Int) -> Unit) {
    (0 until size).forEach(block)
  }

  override fun toString() = "[$width, $height, $depth]"
}