package com.fracturedskies.engine.math

import org.junit.Test
import kotlin.test.assertEquals

class Matrix4Test {

  @Test
  fun itInverts() {
    val m1 = Matrix4(Vector3(1f, 2f, 3f))
    val m2 = Matrix4(Vector3(1f, 2f, 3f)).invert()

    assertEquals(Matrix4.IDENTITY, m1.multiply(m2))
  }
}