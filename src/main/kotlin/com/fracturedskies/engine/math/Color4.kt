package com.fracturedskies.engine.math

import org.lwjgl.BufferUtils

data class Color4(val red: Int, val green: Int, val blue: Int, val alpha: Int) {
  companion object {
    val WHITE = Color4(255, 255, 255, 255)
    val GREEN = Color4(142, 204, 149, 255)
    val DARK_GREEN = Color4(76, 118, 81, 255)
    val BROWN = Color4(178, 161, 130, 255)
    val GRAY = Color4(172, 175, 174, 255)
    val DARK_BROWN = Color4(114, 110, 105, 255)
    val BLACK = Color4(0, 0, 0, 255)
    val WATER = Color4(138, 172, 206, 200)
  }

  fun toFloat(): Float {
    val colorBuffer = BufferUtils.createByteBuffer(4)
    colorBuffer.put(red.toByte())
    colorBuffer.put(green.toByte())
    colorBuffer.put(blue.toByte())
    colorBuffer.put(alpha.toByte())
    colorBuffer.flip()
    return colorBuffer.asFloatBuffer().get(0)
  }

  operator fun div(value: Int) = Color4(red / value, green / value, blue / value, alpha / value)
  operator fun times(value: Int) = Color4(red * value, green * value, blue * value, alpha * value)
}