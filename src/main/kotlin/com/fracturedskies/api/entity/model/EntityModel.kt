package com.fracturedskies.api.entity.model

import com.fracturedskies.engine.math.Vector3
import com.fracturedskies.render.world.Quad


interface EntityModel {
  fun quads(skyLight: Int, blockLight: Int, offset: Vector3, scale: Float): Sequence<Quad>
}
