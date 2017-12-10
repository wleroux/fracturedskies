package com.fracturedskies.game.skylight

import com.fracturedskies.engine.collections.Dimension
import com.fracturedskies.engine.collections.IntMap
import com.fracturedskies.engine.collections.ObjectMap
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.game.BlockType

class LightMap(val dimension: Dimension) {
  val level = IntMap(dimension)
  val type = ObjectMap(dimension, {BlockType.AIR})

  fun has(pos: Vector3i) = has(pos.x, pos.y, pos.z)
  fun has(x: Int, y: Int, z: Int) = dimension.has(x, y, z)
}