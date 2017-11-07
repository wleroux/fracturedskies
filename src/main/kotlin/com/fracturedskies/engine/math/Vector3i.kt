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
  }

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Vector3 -> Math.abs(x - other.x) <= 0.00001f &&
              Math.abs(y - other.y) <= 0.00001f &&
              Math.abs(z - other.z) <= 0.00001f
      else -> false
    }
  }

  operator fun plus(o: Vector3i): Vector3i = Vector3i(x + o.x, y + o.y, z + o.z)
}

infix fun Vector3i.within(world: World): Boolean {
  return x in 0 until world.width && y in 0 until world.height && z in 0 until world.depth
}
