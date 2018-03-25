package com.fracturedskies.engine.math

import com.fracturedskies.engine.collections.*

data class Vector3i private constructor(val x: Int, val y: Int, val z: Int) {
  companion object {
    val AXIS_X = Vector3i(1, 0, 0)
    val AXIS_NEG_X = Vector3i(-1, 0, 0)
    val AXIS_Y = Vector3i(0, 1, 0)
    val AXIS_NEG_Y = Vector3i(0, -1, 0)
    val AXIS_Z = Vector3i(0, 0, 1)
    val AXIS_NEG_Z = Vector3i(0, 0, -1)
    val NEIGHBOURS = listOf(AXIS_X, AXIS_Y, AXIS_Z, AXIS_NEG_X, AXIS_NEG_Y, AXIS_NEG_Z)
    val ADDITIVE_UNIT = Vector3i(0, 0, 0)
    val X_PLANE_NEIGHBORS = listOf(AXIS_X, AXIS_NEG_X)
    val Y_PLANE_NEIGHBORS = listOf(AXIS_Y, AXIS_NEG_Y)
    val Z_PLANE_NEIGHBORS = listOf(AXIS_Z, AXIS_NEG_Z)
    val XY_PLANE_NEIGHBORS = X_PLANE_NEIGHBORS + Y_PLANE_NEIGHBORS
    val XZ_PLANE_NEIGHBORS = X_PLANE_NEIGHBORS + Z_PLANE_NEIGHBORS

    private const val CACHE_X_SIZE = 128
    private const val CACHE_Y_SIZE = 258
    private const val CACHE_Z_SIZE = 128
    private val cache = Array(CACHE_X_SIZE * CACHE_Y_SIZE * CACHE_Z_SIZE, { it -> Vector3i(
            it % CACHE_X_SIZE,
            (it / CACHE_X_SIZE) % CACHE_Y_SIZE,
            (it / CACHE_X_SIZE / CACHE_Y_SIZE) % CACHE_Z_SIZE
    )})

    operator fun invoke(x: Int, y: Int, z: Int): Vector3i {
      return if (x >= 0 && x < CACHE_X_SIZE && y >= 0 && y < CACHE_Y_SIZE && z >= 0 && z < CACHE_Z_SIZE) {
        val index = z * CACHE_Y_SIZE * CACHE_X_SIZE + y * CACHE_X_SIZE + x
        cache[index]
      } else {
        Vector3i(x, y, z)
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Vector3i -> Math.abs(x - other.x) == 0 &&
              Math.abs(y - other.y) == 0 &&
              Math.abs(z - other.z) == 0
      else -> false
    }
  }

  operator fun plus(o: Vector3i) = Vector3i(x + o.x, y + o.y, z + o.z)
  operator fun minus(o: Vector3i) = Vector3i(x - o.x, y - o.y, z - o.z)
  override fun hashCode(): Int {
    var result = x
    result = 31 * result + y
    result = 31 * result + z
    return result
  }

  operator fun div(o: Vector3i) = Vector3i(x / o.x, y / o.y, z / o.z)
  operator fun div(dimension: Dimension) = Vector3i(x / dimension.width, y / dimension.height, z / dimension.depth)
  operator fun rem(dimension: Dimension) = Vector3i(x % dimension.width, y % dimension.height, z % dimension.depth)
  operator fun times(dimension: Dimension) = Vector3i(x * dimension.width, y * dimension.height, z * dimension.depth)
}

infix fun <K> Vector3i.within(map: Space<K>): Boolean = map.has(this)
fun Vector3i.toVector3() = Vector3(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())
