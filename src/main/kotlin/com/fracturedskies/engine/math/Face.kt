package com.fracturedskies.engine.math

import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_NEG_X
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_NEG_Y
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_NEG_Z
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_X
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_Y
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_Z


enum class Face(val dir: Vector3i, val du: Vector3i, val dv: Vector3i) {
  TOP(AXIS_Y, AXIS_X, AXIS_NEG_Z),
  LEFT(AXIS_NEG_X, AXIS_NEG_Z, AXIS_NEG_Y),
  FRONT(AXIS_NEG_Z, AXIS_X, AXIS_NEG_Y),
  RIGHT(AXIS_X, AXIS_Z, AXIS_NEG_Y),
  BACK(AXIS_Z, AXIS_NEG_X, AXIS_NEG_Y),
  BOTTOM(AXIS_NEG_Y, AXIS_X, AXIS_Z)
}