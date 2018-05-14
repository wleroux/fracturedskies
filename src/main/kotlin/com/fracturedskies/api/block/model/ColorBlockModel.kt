package com.fracturedskies.api.block.model

import com.fracturedskies.api.block.Block
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.world.*
import com.fracturedskies.render.world.VertexCorner.*


class ColorBlockModel(val color: Color4): BlockModel {
  override fun quads(world: Space<Block>, pos: Vector3i, sliceMesh: Boolean, offset: Vector3, scale: Float): Sequence<Quad> {
    return Face.values().asSequence()
        .filter { face ->
          if (face == Face.TOP && sliceMesh) {
            true
          } else if (!world[pos].type.opaque) {
            true
          } else {
            val adjPos = pos + face.dir
            if (world.has(adjPos)) !world[adjPos].type.opaque else true
          }
        }
        .flatMap { face ->
      val adjPos = pos + face.dir
      val occlusions = Occlusion.of(world.project { it.type.opaque }, adjPos, face.du, face.dv)

      val fDir = face.dir.toVector3()
      val fPos = pos.toVector3()
      val fDu = face.du.toVector3()
      val fDv = face.dv.toVector3()

      val fOrigin = fPos + Vector3(0.5f, 0.5f, 0.5f) + (fDir / 2f) - (fDu / 2f) - (fDv / 2f)
      listOf(Quad(
          offset.x + fOrigin.x * scale, fDu.x * scale, fDv.x * scale,
          offset.y + fOrigin.y * scale, fDu.y * scale, fDv.y * scale,
          offset.z + fOrigin.z * scale, fDu.z * scale, fDv.z * scale,
          fDir.x, fDir.y, fDir.z,
          TOP_LEFT.skyLightLevel(world, adjPos, face.du, face.dv),
          TOP_RIGHT.skyLightLevel(world, adjPos, face.du, face.dv),
          BOTTOM_RIGHT.skyLightLevel(world, adjPos, face.du, face.dv),
          BOTTOM_LEFT.skyLightLevel(world, adjPos, face.du, face.dv),
          TOP_LEFT.blockLightLevel(world, adjPos, face.du, face.dv),
          TOP_RIGHT.blockLightLevel(world, adjPos, face.du, face.dv),
          BOTTOM_RIGHT.blockLightLevel(world, adjPos, face.du, face.dv),
          BOTTOM_LEFT.blockLightLevel(world, adjPos, face.du, face.dv),
          color,
          TOP_LEFT.occlusionLevel(occlusions),
          TOP_RIGHT.occlusionLevel(occlusions),
          BOTTOM_RIGHT.occlusionLevel(occlusions),
          BOTTOM_LEFT.occlusionLevel(occlusions)
      )).asSequence()
    }
  }
}
