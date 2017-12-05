package com.fracturedskies.game.skylight

import com.fracturedskies.engine.collections.BooleanMap
import com.fracturedskies.engine.collections.Dimension
import com.fracturedskies.engine.collections.IntMap
import com.fracturedskies.engine.math.Vector3i

class SkyLightMap(val dimension: Dimension) {
  val level = IntMap(dimension)
  val opaque = BooleanMap(dimension)

  fun has(pos: Vector3i) = has(pos.x, pos.y, pos.z)
  fun has(x: Int, y: Int, z: Int) = dimension.has(x, y, z)
}