package com.fracturedskies.game.skylight

import com.fracturedskies.engine.math.Vector3i

class SkyLightMap(val width: Int, val height: Int, val depth: Int) {
  private val dimension = Dimension(width, height, depth)
  val level = LevelMap(dimension)
  val opaque = OpaqueMap(dimension)

  class LevelMap(private val dimension: Dimension) {
    private val backend = IntArray(dimension.size) { 0 }

    operator fun get(pos: Vector3i) = get(pos.x, pos.y, pos.z)
    operator fun get(x: Int, y: Int, z: Int) = backend[dimension(x, y, z)]
    operator fun set(x: Int, y: Int, z: Int, value: Int) {
      backend[dimension(x, y, z)] = value
    }
    operator fun set(pos: Vector3i, value: Int) = set(pos.x, pos.y, pos.z, value)
  }

  class OpaqueMap(private val dimension: Dimension) {
    private val backend = BooleanArray(dimension.size) { false }
    operator fun get(pos: Vector3i) = get(pos.x, pos.y, pos.z)
    operator fun get(x: Int, y: Int, z: Int): Boolean {
      return backend[dimension(x, y, z)]
    }
    operator fun set(x: Int, y: Int, z: Int, value: Boolean) {
      backend[dimension(x, y, z)] = value
    }
    operator fun set(pos: Vector3i, value: Boolean) = set(pos.x, pos.y, pos.z, value)
  }

  fun has(pos: Vector3i) = dimension.has(pos)
}