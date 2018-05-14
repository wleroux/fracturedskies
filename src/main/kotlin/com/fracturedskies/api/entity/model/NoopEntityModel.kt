package com.fracturedskies.api.entity.model

import com.fracturedskies.engine.math.Vector3
import com.fracturedskies.render.world.Quad


object NoopEntityModel : EntityModel {
  override fun quads(skyLight: Int, blockLight: Int, offset: Vector3, scale: Float): Sequence<Quad> {
    return emptySequence()
  }
}