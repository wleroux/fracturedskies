package com.fracturedskies.engine.math

data class Quaternion4(var w: Float, var x: Float, var y: Float, var z: Float) {
  companion object {
    operator fun invoke(axis: Vector3, angle: Float): Quaternion4 {
      val s = Math.sin((angle / 2).toDouble()).toFloat()
      val w = Math.cos((angle / 2).toDouble()).toFloat()
      val x = axis.x * s
      val y = axis.y * s
      val z = axis.z * s
      return Quaternion4(w, x, y, z).normalize()
    }
  }

  private val magnitude get() = Math.sqrt((w * w + x * x + y * y + z * z).toDouble()).toFloat()

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

  operator fun times(s: Float): Quaternion4 {
    val halfAngle = Math.acos(w.toDouble()).toFloat()
    val sin = Math.sin(halfAngle.toDouble()).toFloat()
    val ax = x / sin
    val ay = y / sin
    val az = z / sin

    val newHalfAngle = halfAngle * s
    val newSin = Math.sin(newHalfAngle.toDouble()).toFloat()
    return Quaternion4(
            Math.cos(newHalfAngle.toDouble()).toFloat(),
            ax * newSin,
            ay * newSin,
            az * newSin
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

  private fun set(w: Float, x: Float, y: Float, z: Float): Quaternion4 {
    this.w = w
    this.x = x
    this.y = y
    this.z = z
    return this
  }

  fun normalize(): Quaternion4 {
    val m = magnitude
    set(w / m, x / m, y / m, z / m)
    return this
  }
}
