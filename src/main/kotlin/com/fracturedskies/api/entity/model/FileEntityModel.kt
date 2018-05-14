package com.fracturedskies.api.entity.model

import com.fracturedskies.engine.math.Vector3
import com.fracturedskies.render.MeshParser
import com.fracturedskies.render.world.Quad


class FileEntityModel(private val file: String): EntityModel {
  override fun quads(skyLight: Int, blockLight: Int, offset: Vector3, scale: Float): Sequence<Quad> {
    return MeshParser.generateQuads(file, skyLight, blockLight, emptyMap(), offset * scale, 1f/16f * scale)
  }
}