package com.fracturedskies.engine.math

import com.fracturedskies.engine.collections.*
import kotlin.math.abs

data class Vector2i private constructor(val x: Int, val z: Int) {
  companion object {
    val AXIS_X = Vector2i(1, 0)
    val AXIS_NEG_X = Vector2i(-1, 0)
    val AXIS_Z = Vector2i(0, 1)
    val AXIS_NEG_Z = Vector2i(0, -1)
    val NEIGHBOURS = listOf(AXIS_X, AXIS_Z, AXIS_NEG_X, AXIS_NEG_Z)
    val ADDITIVE_UNIT = Vector3i(0, 0, 0)
    val X_PLANE_NEIGHBORS = listOf(AXIS_X, AXIS_NEG_X)
    val Z_PLANE_NEIGHBORS = listOf(AXIS_Z, AXIS_NEG_Z)
    val XZ_PLANE_NEIGHBORS = X_PLANE_NEIGHBORS + Z_PLANE_NEIGHBORS

    private const val CACHE_X_SIZE = 128
    private const val CACHE_Z_SIZE = 128
    private val cache = Array(CACHE_X_SIZE * CACHE_Z_SIZE, { it -> Vector2i(
        it % CACHE_X_SIZE,
        (it / CACHE_X_SIZE) % CACHE_Z_SIZE
    )})

    operator fun invoke(x: Int, z: Int): Vector2i {
      return if (0 <= x && x < CACHE_X_SIZE && 0 <= z && z < CACHE_Z_SIZE) {
        val index = CACHE_Z_SIZE + z * CACHE_X_SIZE + x
        cache[index]
      } else {
        Vector2i(x, z)
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Vector3i -> Math.abs(x - other.x) == 0 &&
          Math.abs(z - other.z) == 0
      else -> false
    }
  }

  operator fun plus(o: Vector2i) = Vector2i(x + o.x, z + o.z)
  operator fun minus(o: Vector2i) = Vector2i(x - o.x, z - o.z)
  override fun hashCode(): Int {
    var result = x
    result = 31 * result + z
    return result
  }

  operator fun div(o: Vector3i) = Vector2i(x / o.x, z / o.z)
  operator fun div(dimension: Dimension) = Vector2i(x / dimension.width, z / dimension.depth)
  operator fun rem(dimension: Dimension) = Vector2i(x % dimension.width, z % dimension.depth)
  operator fun times(dimension: Dimension) = Vector2i(x * dimension.width, z * dimension.depth)
  infix fun distanceTo(o: Vector2i) = abs(this.x - o.x) + abs(this.z - o.z)
  infix fun <K> within(map: Area<K>): Boolean = map.has(this)
}
