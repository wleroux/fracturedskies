package com.fracturedskies.engine.math

import java.lang.Math.sqrt

data class Vector2(var x: Float, var y: Float) {
  val magnitude: Float
    get() = sqrt((x * x + y * y).toDouble()).toFloat()

  companion object {
    val AXIS_X = Vector2(1f, 0f)
    val AXIS_NEG_X = Vector2(-1f, 0f)
    val AXIS_Y = Vector2(0f, 1f)
    val AXIS_NEG_Y = Vector2(0f, -1f)
    val ZERO = Vector2(0f, 0f)
  }

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Vector3 -> Math.abs(x - other.x) <= 0.00001f &&
          Math.abs(y - other.y) <= 0.00001f
      else -> false
    }
  }

  operator fun plus(o: Vector3) = Vector2(this.x + o.x, this.y + o.y)
  operator fun minus(o: Vector3) = Vector2(this.x - o.x, this.y - o.y)
  operator fun times(s: Float) = Vector2(this.x * s, this.y * s)
  fun normalize() = times(1f / magnitude)
}