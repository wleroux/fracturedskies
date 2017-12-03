package com.fracturedskies.engine.math

data class Color4(val red: Int, val green: Int, val blue: Int, val alpha: Int) {
  companion object {
    val WHITE = Color4(255, 255, 255, 255)
    val GREEN = Color4(142, 204, 149, 255)
    val BROWN = Color4(178, 161, 130, 255)
    val BLACK = Color4(0, 0, 0, 255)
  }
}