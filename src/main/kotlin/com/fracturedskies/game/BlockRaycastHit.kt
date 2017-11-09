package com.fracturedskies.game

import com.fracturedskies.engine.math.Vector3
import com.fracturedskies.engine.math.Vector3i

data class BlockRaycastHit(val block: Block, val position: Vector3i, val direction: Vector3, val intersection: Vector3, val normal: Vector3) {
  val faces: List<Vector3i> get() {
    val faces = mutableListOf<Vector3i>()
    if (this.normal.x != 0f)
      faces.add(if (this.normal.x > 0) Vector3i.AXIS_X else Vector3i.AXIS_NEG_X)
    if (this.normal.y != 0f)
      faces.add(if (this.normal.y > 0) Vector3i.AXIS_Y else Vector3i.AXIS_NEG_Y)
    if (this.normal.z != 0f)
      faces.add(if (this.normal.z > 0) Vector3i.AXIS_NEG_Z else Vector3i.AXIS_Z)
    return faces.toList()
  }
}
