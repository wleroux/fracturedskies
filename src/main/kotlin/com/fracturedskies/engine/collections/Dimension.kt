package com.fracturedskies.engine.collections

import com.fracturedskies.engine.math.Vector3i

class Dimension(val width: Int, val height: Int, val depth: Int) {
  val size = width * height * depth
  private val widthDepth = width * depth

  fun has(index: Int) = index in (0 until size)
  fun has(pos: Vector3i) = has(pos.x, pos.y, pos.z)
  fun has(x: Int, y: Int, z: Int) =
      (0 <= x && x < width) && (0 <= y && y < height) && (0 <= z && z < depth)

  operator fun invoke(pos: Vector3i) = invoke(pos.x, pos.y, pos.z)
  operator fun invoke(x: Int, y: Int, z: Int) = (y * widthDepth) + (z * width) + x

  inline fun forEach(block: (Int) -> Unit) {
    indices().forEach(block)
  }
  fun indices() = (0 until size)

  override fun toString() = "[$width, $height, $depth]"
  fun toVector3i(index: Int): Vector3i {
    val x = index % depth % width
    val z = (index / width) % depth
    val y = (index / width / depth)
    return Vector3i(x, y, z)
  }

  operator fun div(dimension: Dimension) = Dimension(width / dimension.width, height / dimension.height, depth / dimension.depth)
}