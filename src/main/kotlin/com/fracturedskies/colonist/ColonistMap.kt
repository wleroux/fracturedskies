package com.fracturedskies.colonist

import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Vector3i


class ColonistMap(val dimension: Dimension) {
  val blocked = BooleanMutableSpace(dimension, {false})
  val colonistPositions = mutableMapOf<Id, Vector3i>()
  val taskDetails = mutableMapOf<Id, Any?>()
  val colonistWork = mutableMapOf<Id, Id?>()
}