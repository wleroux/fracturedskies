package com.fracturedskies.engine.jeact

data class Point(val x: Int, val y: Int) {
  override fun toString(): String = "($x, $y)"
  infix fun within(bounds: Bounds?): Boolean {
    return if (bounds == null) {
      true
    } else {
      bounds.x <= x && x <= bounds.x + bounds.width &&
              bounds.y <= y && y <= bounds.y + bounds.height
    }
  }
}