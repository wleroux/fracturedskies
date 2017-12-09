package com.fracturedskies.game

import com.fracturedskies.engine.math.Color4
import com.fracturedskies.engine.math.clamp
import com.fracturedskies.engine.math.lerp
import com.fracturedskies.game.skylight.SkyLightSystem
import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlin.math.floor

class LightMap(private val colorSwatches: Map<Int, List<Color4>>) {

  private fun index(timeOfDay: Float) = clamp((timeOfDay * colorSwatches.size).toInt(), 0 until colorSwatches.size )
  operator fun get(timeOfDay: Float) = colorSwatches[index(timeOfDay)]!!

  fun getColorBuffer(timeOfDay: Float): IntBuffer {
    val index = (timeOfDay * colorSwatches.size)

    val start = colorSwatches[index.toInt()]!!
    val end = colorSwatches[(index.toInt() + 1) % colorSwatches.size]!!
    val alpha = index - floor(index)

    val colors = start.asSequence().zip(end.asSequence()) { a, b -> lerp(alpha, a, b) }.toList()
    val colorsBuffer = BufferUtils.createIntBuffer(4 * colors.size)
    colors.forEach {
      colorsBuffer.put(it.red)
      colorsBuffer.put(it.green)
      colorsBuffer.put(it.blue)
      colorsBuffer.put(it.alpha)
    }
    colorsBuffer.flip()
    return colorsBuffer
  }


  companion object {
    fun load(rawImageBuffer: ByteBuffer, timeSegments: Int): LightMap {
      val imageBuffer = STBImage.stbi_load_from_memory(rawImageBuffer, intArrayOf(timeSegments), intArrayOf(SkyLightSystem.MAX_SKYLIGHT_LEVEL), intArrayOf(3), 4)
      return LightMap((0 until timeSegments).map { timeOfDay ->
        val colorSwatch = (0..SkyLightSystem.MAX_SKYLIGHT_LEVEL).map { lightLevel ->
          val offset = 4 * (lightLevel * timeSegments + timeOfDay)
          Color4(
              imageBuffer.get(offset + 0).toInt() and 0xFF,
              imageBuffer.get(offset + 1).toInt() and 0xFF,
              imageBuffer.get(offset + 2).toInt() and 0xFF,
              imageBuffer.get(offset + 3).toInt() and 0xFF
          )
        }
        timeOfDay to colorSwatch
      }.toMap())
    }
  }
}