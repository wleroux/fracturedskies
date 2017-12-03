package com.fracturedskies.render.components.world

import com.fracturedskies.engine.math.Color4
import org.lwjgl.BufferUtils

/**
 * Builds a mesh by constructing Quads.
 */
data class Quad(
        private val x: Float, private val dwx: Float, private val dhx: Float,
        private val y: Float, private val dwy: Float, private val dhy: Float,
        private val z: Float, private val dwz: Float, private val dhz: Float,
        private val nx: Float, private val ny: Float, private val nz: Float,
        private var color: Color4
) {

  fun indices(offset: Int): IntArray {
    return intArrayOf(
            offset + 0, offset + 1, offset + 2,
            offset + 2, offset + 3, offset + 0
    )
  }

  fun vertices(): FloatArray {
    val colorBuffer = BufferUtils.createByteBuffer(4)
    colorBuffer.put(color.red.toByte())
    colorBuffer.put(color.green.toByte())
    colorBuffer.put(color.blue.toByte())
    colorBuffer.put(color.alpha.toByte())
    colorBuffer.flip()
    val colorAsFloat = colorBuffer.asFloatBuffer().get(0)

    return floatArrayOf(
            x +        0f, y +        0f, z +        0f, colorAsFloat, nx, ny, nz,
            x + dwx      , y + dwy      , z + dwz      , colorAsFloat, nx, ny, nz,
            x + dwx + dhx, y + dwy + dhy, z + dwz + dhz, colorAsFloat, nx, ny, nz,
            x +       dhx, y +       dhy, z +       dhz, colorAsFloat, nx, ny, nz
    )
  }
}