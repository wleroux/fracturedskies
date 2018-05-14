package com.fracturedskies.api.entity.model

import com.fracturedskies.engine.math.*
import com.fracturedskies.render.world.Quad


class ColorEntityModel(val color: Color4): EntityModel {
  override fun quads(skyLight: Int, blockLight: Int, offset: Vector3, scale: Float): Sequence<Quad> {
    return Face.values().asSequence()
        .flatMap { face ->
      val fDir = face.dir.toVector3()
      val fPos = Vector3.ZERO
      val fDu = face.du.toVector3()
      val fDv = face.dv.toVector3()
      val fOrigin = fPos + Vector3(0.5f, 0.5f, 0.5f) + (fDir / 2f) - (fDu / 2f) - (fDv / 2f)
      listOf(Quad(
          offset.x + fOrigin.x * scale, fDu.x * scale, fDv.x * scale,
          offset.y + fOrigin.y * scale, fDu.y * scale, fDv.y * scale,
          offset.z + fOrigin.z * scale, fDu.z * scale, fDv.z * scale,
          fDir.x, fDir.y, fDir.z,
          skyLight.toFloat(), skyLight.toFloat(), skyLight.toFloat(), skyLight.toFloat(),
          blockLight.toFloat(), blockLight.toFloat(), blockLight.toFloat(), blockLight.toFloat(),
          color,
          0f, 0f, 0f, 0f
      )).asSequence()
    }
  }
}
