package com.fracturedskies.engine.collections

import com.fracturedskies.engine.math.Vector3i

class Dimension(val width: Int, val height: Int, val depth: Int) {
  val size = width * height * depth
  private val widthDepth = width * depth
  fun has(index: Int) = index in 0 until size
  fun has(pos: Vector3i) = has(pos.x, pos.y, pos.z)
  fun has(x: Int, y: Int, z: Int) =
      (x in 0 until width) && (y in 0 until height) && (z in 0 until depth)
  inline fun forEach(block: (Int) -> Unit) {
    indices().forEach(block)
  }
  fun indices() = (0 until size)

  fun index(pos: Vector3i) = index(pos.x, pos.y, pos.z)
  fun index(x: Int, y: Int, z: Int): Int = (y * widthDepth) + (z * width) + x
  fun vector3i(index: Int): Vector3i {
    val x = index % depth % width
    val z = (index / width) % depth
    val y = (index / width / depth)
    return Vector3i(x, y, z)
  }

  operator fun div(dimension: Dimension) = Dimension(width / dimension.width, height / dimension.height, depth / dimension.depth)
  override fun toString() = "[$width, $height, $depth]"
}

interface HasDimension {
  val dimension: Dimension
  val width get() = dimension.width
  val height get() = dimension.height
  val depth get() = dimension.depth
  fun vector3i(index: Int) = dimension.vector3i(index)
  fun index(x: Int, y: Int, z: Int) = dimension.index(x, y, z)
  fun index(pos: Vector3i) = dimension.index(pos)
  fun has(pos: Vector3i) = dimension.has(pos)
  fun has(x: Int, y: Int, z: Int) = dimension.has(x, y, z)
  fun has(index: Int) = dimension.has(index)
}