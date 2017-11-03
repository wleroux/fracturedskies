package com.fracturedskies.engine.math

data class Color4(val red: Int, val green: Int, val blue: Int, val alpha: Int) {
  companion object {
    val WHITE = Color4(255, 255, 255, 255)
  }
}