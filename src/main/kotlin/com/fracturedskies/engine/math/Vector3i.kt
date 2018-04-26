package com.fracturedskies.engine.math

import com.fracturedskies.engine.collections.*
import kotlin.math.*

data class Vector3i private constructor(val x: Int, val y: Int, val z: Int) {
  val magnitude: Float get() = sqrt((x * x + y * y + z * z).toFloat())

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

    fun area(xRange: IntRange, yRange: IntRange, zRange: IntRange): List<Vector3i> {
      return xRange.flatMap { x ->
        zRange.flatMap { z ->
          yRange.map { y ->
            Vector3i(x, y, z)
          }
        }
      }
    }

    fun xZRing(radius: Int): List<Vector3i> {
      return if (radius == 0) {
        listOf(Vector3i(0, 0, 0))
      } else {
        val ring = mutableListOf<Vector3i>()
        (-radius..radius).forEach { dx ->
          ring.add(Vector3i(dx, 0, radius))
          ring.add(Vector3i(dx, 0, -radius))
        }
        ((-radius+1)..(radius-1)).forEach { dz ->
          ring.add(Vector3i(radius, 0, dz))
          ring.add(Vector3i(-radius, 0, dz))
        }
        ring
      }
    }
    fun xZSpiral(radius: Int): List<Vector3i> {
      return (0..radius).flatMap{ xZRing(it) }
    }

    private const val CACHE_X_SIZE = 128
    private const val CACHE_Y_SIZE = 258
    private const val CACHE_Z_SIZE = 128
    private val cache = Array(CACHE_X_SIZE * CACHE_Y_SIZE * CACHE_Z_SIZE, { it -> Vector3i(
            it % CACHE_X_SIZE,
            (it / CACHE_X_SIZE / CACHE_Z_SIZE) % CACHE_Y_SIZE,
            (it / CACHE_X_SIZE) % CACHE_Z_SIZE
    )})

    operator fun invoke(x: Int, y: Int, z: Int): Vector3i {
      return if (0 <= x && x < CACHE_X_SIZE && 0 <= y && y < CACHE_Y_SIZE && 0 <= z && z < CACHE_Z_SIZE) {
        val index = y * CACHE_X_SIZE * CACHE_Z_SIZE + z * CACHE_X_SIZE + x
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
  operator fun times(s: Int): Vector3i = Vector3i(x * s, y * s, z * s)
}

infix fun Vector3i.distanceTo(o: Vector3i) =
    abs(this.x - o.x) + abs(this.y - o.y) + abs(this.z - o.z)
infix fun <K> Vector3i.within(map: Space<K>): Boolean = map.has(this)
fun Vector3i.toVector3() = Vector3(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())
