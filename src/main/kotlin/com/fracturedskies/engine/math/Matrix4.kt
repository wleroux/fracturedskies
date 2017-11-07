package com.fracturedskies.engine.math

import java.nio.FloatBuffer

/**
 *
 */
data class Matrix4(
    val m00: Float, val m01: Float, val m02: Float, val m03: Float,
    val m10: Float, val m11: Float, val m12: Float, val m13: Float,
    val m20: Float, val m21: Float, val m22: Float, val m23: Float,
    val m30: Float, val m31: Float, val m32: Float, val m33: Float) {
  companion object {
    val IDENTITY = Matrix4(
      1f, 0f, 0f, 0f,
      0f, 1f, 0f, 0f,
      0f, 0f, 1f, 0f,
      0f, 0f, 0f, 1f
    )
    operator fun invoke(position: Vector3 = Vector3(0f, 0f, 0f), rotation: Quaternion4 = Quaternion4(1f, 0f, 0f, 0f)): Matrix4 {
      val xx = rotation.x * rotation.x
      val xy = rotation.x * rotation.y
      val xz = rotation.x * rotation.z
      val xw = rotation.x * rotation.w

      val yy = rotation.y * rotation.y
      val yz = rotation.y * rotation.z
      val yw = rotation.y * rotation.w

      val zz = rotation.z * rotation.z
      val zw = rotation.z * rotation.w

      //@formatter:off
      return Matrix4(
              1 - 2 * (yy + zz), 2 * (xy - zw), 2 * (xz + yw), position.x,
              2 * (xy + zw), 1 - 2 * (xx + zz), 2 * (yz - xw), position.y,
              2 * (xz - yw), 2 * (yz + xw), 1 - 2 * (xx + yy), position.z,
              0f, 0f, 0f, 1f
      )
      //@formatter:on
    }


    fun orthogonal(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Matrix4 {
      val width = right - left
      val height = top - bottom
      val depth = far - near
      //@formatter:off
      return Matrix4(
              2 / width,         0f,        0f,  -(left + right) / width,
                     0f, 2 / height,        0f, -(top + bottom) / height,
                     0f,         0f, 2 / depth,    -(far + near) / depth,
                     0f,         0f,        0f,                        1f
      )
      //@formatter:on
    }


    fun perspective(fov: Float, width: Int, height: Int, near: Float, far: Float): Matrix4 {
      val angle = Math.tan((fov / 2).toDouble()).toFloat()
      val aspect = width.toFloat() / height.toFloat()
      //@formatter:off
      return Matrix4(
              1f / (aspect * angle),         0f,                           0f,                             0f,
                                 0f, 1f / angle,                           0f,                             0f,
                                 0f,         0f, (-near - far) / (near - far), 2f * near * far / (near - far),
                                 0f,         0f,                           1f,                             0f
      )
      //@formatter:on
    }
  }

  private val determinant: Float
    get() {
      return m03 * m12 * m21 * m30 - m02 * m13 * m21 * m30 - m03 * m11 * m22 * m30 + m01 * m13 * m22 * m30 +
              m02 * m11 * m23 * m30 - m01 * m12 * m23 * m30 - m03 * m12 * m20 * m31 + m02 * m13 * m20 * m31 +
              m03 * m10 * m22 * m31 - m00 * m13 * m22 * m31 - m02 * m10 * m23 * m31 + m00 * m12 * m23 * m31 +
              m03 * m11 * m20 * m32 - m01 * m13 * m20 * m32 - m03 * m10 * m21 * m32 + m00 * m13 * m21 * m32 +
              m01 * m10 * m23 * m32 - m00 * m11 * m23 * m32 - m02 * m11 * m20 * m33 + m01 * m12 * m20 * m33 +
              m02 * m10 * m21 * m33 - m00 * m12 * m21 * m33 - m01 * m10 * m22 * m33 + m00 * m11 * m22 * m33
    }

  override fun toString(): String =
    "[$m00, $m01, $m02, $m03, $m10, $m11, $m12, $m13, $m20, $m21, $m22, $m23, $m30, $m31, $m32, $m33]"

  fun store(buffer: FloatBuffer) {
    buffer.put(m00).put(m10).put(m20).put(m30)
    buffer.put(m01).put(m11).put(m21).put(m31)
    buffer.put(m02).put(m12).put(m22).put(m32)
    buffer.put(m03).put(m13).put(m23).put(m33)
  }


  private fun adjoint(): Matrix4 {
    return Matrix4(
      m12 * m23 * m31 - m13 * m22 * m31 + m13 * m21 * m32 - m11 * m23 * m32 - m12 * m21 * m33 + m11 * m22 * m33,
      m03 * m22 * m31 - m02 * m23 * m31 - m03 * m21 * m32 + m01 * m23 * m32 + m02 * m21 * m33 - m01 * m22 * m33,
      m02 * m13 * m31 - m03 * m12 * m31 + m03 * m11 * m32 - m01 * m13 * m32 - m02 * m11 * m33 + m01 * m12 * m33,
      m03 * m12 * m21 - m02 * m13 * m21 - m03 * m11 * m22 + m01 * m13 * m22 + m02 * m11 * m23 - m01 * m12 * m23,

      m13 * m22 * m30 - m12 * m23 * m30 - m13 * m20 * m32 + m10 * m23 * m32 + m12 * m20 * m33 - m10 * m22 * m33,
      m02 * m23 * m30 - m03 * m22 * m30 + m03 * m20 * m32 - m00 * m23 * m32 - m02 * m20 * m33 + m00 * m22 * m33,
      m03 * m12 * m30 - m02 * m13 * m30 - m03 * m10 * m32 + m00 * m13 * m32 + m02 * m10 * m33 - m00 * m12 * m33,
      m02 * m13 * m20 - m03 * m12 * m20 + m03 * m10 * m22 - m00 * m13 * m22 - m02 * m10 * m23 + m00 * m12 * m23,

      m11 * m23 * m30 - m13 * m21 * m30 + m13 * m20 * m31 - m10 * m23 * m31 - m11 * m20 * m33 + m10 * m21 * m33,
      m03 * m21 * m30 - m01 * m23 * m30 - m03 * m20 * m31 + m00 * m23 * m31 + m01 * m20 * m33 - m00 * m21 * m33,
      m01 * m13 * m30 - m03 * m11 * m30 + m03 * m10 * m31 - m00 * m13 * m31 - m01 * m10 * m33 + m00 * m11 * m33,
      m03 * m11 * m20 - m01 * m13 * m20 - m03 * m10 * m21 + m00 * m13 * m21 + m01 * m10 * m23 - m00 * m11 * m23,

      m12 * m21 * m30 - m11 * m22 * m30 - m12 * m20 * m31 + m10 * m22 * m31 + m11 * m20 * m32 - m10 * m21 * m32,
      m01 * m22 * m30 - m02 * m21 * m30 + m02 * m20 * m31 - m00 * m22 * m31 - m01 * m20 * m32 + m00 * m21 * m32,
      m02 * m11 * m30 - m01 * m12 * m30 - m02 * m10 * m31 + m00 * m12 * m31 + m01 * m10 * m32 - m00 * m11 * m32,
      m01 * m12 * m20 - m02 * m11 * m20 + m02 * m10 * m21 - m00 * m12 * m21 - m01 * m10 * m22 + m00 * m11 * m22
    )
  }

  fun invert(): Matrix4 {
    return adjoint() * (1f / determinant)
  }

  operator fun times(c: Float): Matrix4 {
    return Matrix4(
      c * m00, c * m01, c * m02, c * m03,
      c * m10, c * m11, c * m12, c * m13,
      c * m20, c * m21, c * m22, c * m23,
      c * m30, c * m31, c * m32, c * m33
    )
  }

  fun multiply(m: Matrix4): Matrix4 {
    return Matrix4(
      m00 * m.m00 + m01 * m.m10 + m02 * m.m20 + m03 * m.m30, m00 * m.m01 + m01 * m.m11 + m02 * m.m21 + m03 * m.m31, m00 * m.m02 + m01 * m.m12 + m02 * m.m22 + m03 * m.m32, m00 * m.m03 + m01 * m.m13 + m02 * m.m23 + m03 * m.m33,
      m10 * m.m00 + m11 * m.m10 + m12 * m.m20 + m13 * m.m30, m10 * m.m01 + m11 * m.m11 + m12 * m.m21 + m13 * m.m31, m10 * m.m02 + m11 * m.m12 + m12 * m.m22 + m13 * m.m32, m10 * m.m03 + m11 * m.m13 + m12 * m.m23 + m13 * m.m33,
      m20 * m.m00 + m21 * m.m10 + m22 * m.m20 + m23 * m.m30, m20 * m.m01 + m21 * m.m11 + m22 * m.m21 + m23 * m.m31, m20 * m.m02 + m21 * m.m12 + m22 * m.m22 + m23 * m.m32, m20 * m.m03 + m21 * m.m13 + m22 * m.m23 + m23 * m.m33,
      m30 * m.m00 + m31 * m.m10 + m32 * m.m20 + m33 * m.m30, m30 * m.m01 + m31 * m.m11 + m32 * m.m21 + m33 * m.m31, m30 * m.m02 + m31 * m.m12 + m32 * m.m22 + m33 * m.m32, m30 * m.m03 + m31 * m.m13 + m32 * m.m23 + m33 * m.m33
    )
  }

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Matrix4 ->
        Math.abs(m00 - other.m00) <= 0.00001f && Math.abs(m01 - other.m01) <= 0.00001f && Math.abs(m02 - other.m02) <= 0.00001f && Math.abs(m03 - other.m03) <= 0.00001f &&
        Math.abs(m10 - other.m10) <= 0.00001f && Math.abs(m11 - other.m11) <= 0.00001f && Math.abs(m12 - other.m12) <= 0.00001f && Math.abs(m13 - other.m13) <= 0.00001f &&
        Math.abs(m20 - other.m20) <= 0.00001f && Math.abs(m21 - other.m21) <= 0.00001f && Math.abs(m22 - other.m22) <= 0.00001f && Math.abs(m23 - other.m23) <= 0.00001f &&
        Math.abs(m30 - other.m30) <= 0.00001f && Math.abs(m31 - other.m31) <= 0.00001f && Math.abs(m32 - other.m32) <= 0.00001f && Math.abs(m33 - other.m33) <= 0.00001f
      else -> false
    }
  }
}