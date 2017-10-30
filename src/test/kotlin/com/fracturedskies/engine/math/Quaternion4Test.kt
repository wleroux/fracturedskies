package com.fracturedskies.engine.math

import org.junit.Test
import kotlin.test.assertEquals

class Quaternion4Test {

  companion object {
    val AXIS_POS_X = Vector3(+1f, 0f, 0f)
    val AXIS_POS_Y = Vector3(0f, +1f, 0f)
    val AXIS_POS_Z = Vector3(0f, 0f, +1f)
    val AXIS_NEG_X = Vector3(-1f, 0f, 0f)
    val AXIS_NEG_Y = Vector3(0f, -1f, 0f)
    val AXIS_NEG_Z = Vector3(0f, 0f, -1f)
    val AXIS_NONE = Vector3(0f, 0f, 0f)
  }

  @Test
  fun itRotatesVertices() {
    val rotation = Quaternion4(Vector3(1f, 0f, 0f), Math.PI.toFloat() / 2f)

    assertEquals(AXIS_NONE, AXIS_NONE * rotation)

    assertEquals(AXIS_POS_X, AXIS_POS_X * rotation)
    assertEquals(AXIS_NEG_X, AXIS_NEG_X * rotation)

    assertEquals(AXIS_POS_Z, AXIS_POS_Y * rotation)
    assertEquals(AXIS_NEG_Y, AXIS_POS_Z * rotation)
    assertEquals(AXIS_NEG_Z, AXIS_NEG_Y * rotation)
    assertEquals(AXIS_POS_Y, AXIS_NEG_Z * rotation)
  }

  @Test
  fun itRotatesAsMatrices() {
    val rotation = Matrix4(rotation = Quaternion4(Vector3(1f, 0f, 0f), Math.PI.toFloat() / 2f))

    assertEquals(AXIS_NONE, AXIS_NONE * rotation)

    assertEquals(AXIS_POS_X, AXIS_POS_X * rotation)
    assertEquals(AXIS_NEG_X, AXIS_NEG_X * rotation)

    assertEquals(AXIS_POS_Z, AXIS_POS_Y * rotation)
    assertEquals(AXIS_NEG_Y, AXIS_POS_Z * rotation)
    assertEquals(AXIS_NEG_Z, AXIS_NEG_Y * rotation)
    assertEquals(AXIS_POS_Y, AXIS_NEG_Z * rotation)
  }


  @Test
  fun itConjugates() {
    val rotation = Quaternion4(Vector3(1f, 0f, 0f), Math.PI.toFloat() / 2f)
    val negRotation = rotation.conjugate()

    assertEquals(AXIS_NONE, AXIS_NONE * rotation * negRotation)

    assertEquals(AXIS_POS_X, AXIS_POS_X * rotation * negRotation)
    assertEquals(AXIS_NEG_X, AXIS_NEG_X * rotation * negRotation)

    assertEquals(AXIS_POS_Y, AXIS_POS_Y * rotation * negRotation)
    assertEquals(AXIS_POS_Z, AXIS_POS_Z * rotation * negRotation)
    assertEquals(AXIS_NEG_Y, AXIS_NEG_Y * rotation * negRotation)
    assertEquals(AXIS_NEG_Z, AXIS_NEG_Z * rotation * negRotation)
  }

  @Test
  fun itMultiplies() {
    listOf(AXIS_POS_X, AXIS_POS_Y, AXIS_POS_Z, AXIS_NEG_X, AXIS_NEG_Y, AXIS_NEG_Z, AXIS_NONE ).forEach({ vector ->
      val rotation1 = Quaternion4(vector, Math.PI.toFloat() / 4f)
      val rotation2 = Quaternion4(vector, Math.PI.toFloat() / 3f)
      val finalRotation = Quaternion4(vector, Math.PI.toFloat() * 7f / 12f)

      assertEquals(finalRotation, rotation1 * rotation2)
    })
  }
}