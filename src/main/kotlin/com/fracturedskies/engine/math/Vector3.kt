package com.fracturedskies.engine.math

data class Vector3(val x: Float, val y: Float, val z: Float) {
  companion object {
    val AXIS_X = Vector3(1f, 0f, 0f)
    val AXIS_NEG_X = Vector3(-1f, 0f, 0f)
    val AXIS_Y = Vector3(0f, 1f, 0f)
    val AXIS_NEG_Y = Vector3(0f, -1f, 0f)
    val AXIS_Z = Vector3(0f, 0f, 1f)
    val AXIS_NEG_Z = Vector3(0f, 0f, -1f)
  }

  operator fun times(mat4: Matrix4): Vector3 {
    return Vector3(
      x * mat4.m00 + y * mat4.m01 + z * mat4.m02 + mat4.m03,
      x * mat4.m10 + y * mat4.m11 + z * mat4.m12 + mat4.m13,
      x * mat4.m20 + y * mat4.m21 + z * mat4.m22 + mat4.m23
    )
  }

  operator fun times(q: Quaternion4): Vector3 {
    val x2 = q.x * 2f
    val y2 = q.y * 2f
    val z2 = q.z * 2f
    val xx2 = q.x * x2
    val xy2 = q.x * y2
    val xz2 = q.x * z2
    val xw2 = q.w * x2
    val yy2 = q.y * y2
    val yz2 = q.y * z2
    val yw2 = q.w * y2
    val zz2 = q.z * z2
    val zw2 = q.w * z2

    return Vector3(
      (1f - (yy2 + zz2)) * x + (xy2 - zw2) * y + (xz2 + yw2) * z,
      (xy2 + zw2) * x + (1f - (xx2 + zz2)) * y + (yz2 - xw2) * z,
      (xz2 - yw2) * x + (yz2 + xw2) * y + (1f - (xx2 + yy2)) * z
    )
  }

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Vector3 -> Math.abs(x - other.x) <= 0.00001f &&
              Math.abs(y - other.y) <= 0.00001f &&
              Math.abs(z - other.z) <= 0.00001f
      else -> false
    }
  }
}