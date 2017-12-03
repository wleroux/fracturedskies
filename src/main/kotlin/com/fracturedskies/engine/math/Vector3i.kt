package com.fracturedskies.engine.math

import com.fracturedskies.game.World

data class Vector3i(val x: Int, val y: Int, val z: Int) {
  companion object {
    val AXIS_X = Vector3i(1, 0, 0)
    val AXIS_NEG_X = Vector3i(-1, 0, 0)
    val AXIS_Y = Vector3i(0, 1, 0)
    val AXIS_NEG_Y = Vector3i(0, -1, 0)
    val AXIS_Z = Vector3i(0, 0, 1)
    val AXIS_NEG_Z = Vector3i(0, 0, -1)
    val NEIGHBOURS = listOf(AXIS_X, AXIS_Y, AXIS_Z, AXIS_NEG_X, AXIS_NEG_Y, AXIS_NEG_Z)
    val ADDITIVE_UNIT = Vector3i(0, 0, 0)
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
}

infix fun Vector3i.within(world: World): Boolean = world.has(x, y, z)
