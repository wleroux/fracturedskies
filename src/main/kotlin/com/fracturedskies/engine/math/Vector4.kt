package com.fracturedskies.engine.math

data class Vector4(val x: Float, val y: Float, val z: Float, val w: Float) {
  val magnitude: Float
    get() = Math.sqrt((x * x + y * y + z * z + w * w).toDouble()).toFloat()
  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Vector4 -> Math.abs(x - other.x) <= 0.00001f &&
              Math.abs(y - other.y) <= 0.00001f &&
              Math.abs(z - other.z) <= 0.00001f &&
              Math.abs(w - other.w) <= 0.00001f
      else -> false
    }
  }

  operator fun plus(o: Vector4) = Vector4(this.x + o.x, this.y + o.y, this.z + o.z, this.w + o.w)
  operator fun times(s: Float) = Vector4(this.x * s, this.y * s, this.z * s, this.w * s)
  operator fun times(mat4: Matrix4): Vector4 {
    return Vector4(
            x * mat4.m00 + y * mat4.m01 + z * mat4.m02 + w * mat4.m03,
            x * mat4.m10 + y * mat4.m11 + z * mat4.m12 + w * mat4.m13,
            x * mat4.m20 + y * mat4.m21 + z * mat4.m22 + w * mat4.m23,
            x * mat4.m30 + y * mat4.m31 + z * mat4.m32 + w * mat4.m33
    )
  }
  fun normalize(): Vector4 {
    return times(1f / magnitude)
  }
}