package com.fracturedskies.render.components.world

import com.fracturedskies.engine.math.Color4
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.render.shaders.Mesh
import java.util.*

/**
 * Builds a mesh by constructing Quads.
 */
data class Quad(
        private val x: Float, private val dwx: Float, private val dhx: Float,
        private val y: Float, private val dwy: Float, private val dhy: Float,
        private val z: Float, private val dwz: Float, private val dhz: Float,
        private val nx: Float, private val ny: Float, private val nz: Float,
        private val skyLight: Int, private val blockLight: Int,
        private val color: Color4,
        val topLeftOcclusion: Float, val topRightOcclusion: Float,
        val bottomRightOcclusion: Float, val bottomLeftOcclusion: Float
) {
  companion object {
    operator fun invoke(pos: IntArray, du: IntArray, dv: IntArray, normal: Vector3i, skyLight: Int, blockLight: Int, color: Color4, occlusions: EnumSet<Occlusion>): Quad {
      return Quad(
              pos[0].toFloat(), du[0].toFloat(), dv[0].toFloat(),
              pos[1].toFloat(), du[1].toFloat(), dv[1].toFloat(),
              pos[2].toFloat(), du[2].toFloat(), dv[2].toFloat(),
              normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat(),
              skyLight, blockLight,
              color,
              VertexCorner.TOP_LEFT.occlusionLevel(occlusions),
              VertexCorner.TOP_RIGHT.occlusionLevel(occlusions),
              VertexCorner.BOTTOM_RIGHT.occlusionLevel(occlusions),
              VertexCorner.BOTTOM_LEFT.occlusionLevel(occlusions)
      )
    }
    val Attributes = listOf(Mesh.Attribute.POSITION, Mesh.Attribute.COLOR, Mesh.Attribute.SKY_LIGHT_LEVEL, Mesh.Attribute.BLOCK_LIGHT_LEVEL, Mesh.Attribute.OCCLUSION, Mesh.Attribute.NORMAL)
  }

  fun indices(offset: Int): IntArray {
    return intArrayOf(
            offset + 0, offset + 1, offset + 2,
            offset + 2, offset + 3, offset + 0
    )
  }

  fun vertices(): FloatArray {
    val colorAsFloat = color.toFloat()
    return floatArrayOf(
            x +        0f, y +        0f, z +        0f, colorAsFloat, skyLight.toFloat(), blockLight.toFloat(), topLeftOcclusion, nx, ny, nz,
            x + dwx      , y + dwy      , z + dwz      , colorAsFloat, skyLight.toFloat(), blockLight.toFloat(), topRightOcclusion, nx, ny, nz,
            x + dwx + dhx, y + dwy + dhy, z + dwz + dhz, colorAsFloat, skyLight.toFloat(), blockLight.toFloat(), bottomRightOcclusion, nx, ny, nz,
            x +       dhx, y +       dhy, z +       dhz, colorAsFloat, skyLight.toFloat(), blockLight.toFloat(), bottomLeftOcclusion, nx, ny, nz
    )
  }

  fun reverse() = Quad(
          x + dwx, -dwx, dhx,
          y + dwy, -dwy, dhy,
          z + dwz, -dwz, dhz,
          -nx, -ny, -nz,
          skyLight, blockLight,
          color,
          topRightOcclusion, topLeftOcclusion, bottomLeftOcclusion, bottomRightOcclusion
  )

  fun rotate() = Quad(
          x + dwx, dhx, -dwx,
          y + dwy, dhy, -dwy,
          z + dwz, dhz, -dwz,
          nx, ny, nz,
          skyLight, blockLight,
          color,
          topRightOcclusion, bottomRightOcclusion, bottomLeftOcclusion, topLeftOcclusion
  )
}