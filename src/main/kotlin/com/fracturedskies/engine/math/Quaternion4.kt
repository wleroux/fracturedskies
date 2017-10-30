package com.fracturedskies.engine.math

data class Quaternion4(val w: Float, val x: Float, val y: Float, val z: Float) {
  companion object {
    operator fun invoke(axis: Vector3, angle: Float): Quaternion4 {
      val s = Math.sin((angle / 2).toDouble()).toFloat()
      val w = Math.cos((angle / 2).toDouble()).toFloat()
      val x = axis.x * s
      val y = axis.y * s
      val z = axis.z * s
      val magnitude = Math.pow((w * w + x * x + y * y + z * z).toDouble(), 0.5).toFloat()
      return Quaternion4(
        w / magnitude,
        x / magnitude,
        y / magnitude,
        z / magnitude
      )
    }
  }

  fun conjugate(): Quaternion4 {
    return Quaternion4(w, -x, -y, -z)
  }

  operator fun times(o: Quaternion4): Quaternion4 {
    return Quaternion4(
      w * o.w - x * o.x - y * o.y - z * o.z,
      w * o.x + x * o.w - y * o.z + z * o.y,
      w * o.y + x * o.z + y * o.w - z * o.x,
      w * o.z - x * o.y + y * o.x + z * o.w
    )
  }

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Quaternion4 -> Math.abs(w - other.w) <= 0.00001f &&
              Math.abs(x - other.x) <= 0.00001f &&
              Math.abs(y - other.y) <= 0.00001f &&
              Math.abs(z - other.z) <= 0.00001f
      else -> false
    }
  }
}
