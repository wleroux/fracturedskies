package com.fracturedskies.render.common.components

import com.fracturedskies.render.common.shaders.Texture
import org.lwjgl.BufferUtils
import org.lwjgl.BufferUtils.createByteBuffer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_R8
import org.lwjgl.stb.*
import org.lwjgl.stb.STBTruetype.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import kotlin.math.roundToInt


class TrueTypeFont private constructor(private val font: String, private val fontHeight: Int) {
  companion object {
    private const val BITMAP_W = 512
    private const val BITMAP_H = 512

    private val cache = mutableMapOf<Pair<String, Int>, TrueTypeFont>()
    operator fun invoke(font: String, fontHeight: Int): TrueTypeFont = cache.computeIfAbsent(font to fontHeight) { _ ->
      TrueTypeFont(font, fontHeight)
    }

    private fun getFontByteBuffer(font: String): ByteBuffer {
      TrueTypeFont::class.java.getResourceAsStream(font).use { stream ->
        val byteArray = stream.readBytes()
        val byteBuffer = BufferUtils.createByteBuffer(byteArray.size)
        byteBuffer.put(byteArray)
        byteBuffer.flip()
        return byteBuffer
      }
    }
  }

  private var ascent: Int = 0
  private var descent: Int = 0
  private var lineGap: Int = 0
  private var heightScale: Float = 0f

  lateinit var texture: Texture
  private lateinit var cdata: STBTTBakedChar.Buffer

  init {
    STBTTPackContext.malloc().use {
      MemoryStack.stackPush().use { stack ->
        val ttf = getFontByteBuffer(font)
        val info = STBTTFontinfo.create()
        if (!STBTruetype.stbtt_InitFont(info, ttf))
          throw IllegalStateException("Could not initialize font")

        heightScale = stbtt_ScaleForPixelHeight(info, fontHeight.toFloat())

        val pAscent = stack.mallocInt(1)
        val pDescent = stack.mallocInt(1)
        val pLineGap = stack.mallocInt(1)
        stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap)
        ascent = pAscent[0]
        descent = pDescent[0]
        lineGap = pLineGap[0]

        cdata = STBTTBakedChar.malloc(96)

        val bitmap = createByteBuffer(BITMAP_W * BITMAP_H)
        stbtt_BakeFontBitmap(ttf, fontHeight.toFloat(), bitmap, BITMAP_W, BITMAP_H, 32, cdata)
        texture = Texture(BITMAP_W, BITMAP_H, bitmap, GL_R8, GL_RED, GL_UNSIGNED_BYTE)
      }
    }
  }

  fun getVertices(text: String): List<TextVertex> {
    STBTTPackContext.malloc().use {
      MemoryStack.stackPush().use { stack ->
        val baseline = -descent * heightScale
        val x = stack.floats(0f)
        val y = stack.floats(0f)
        val quad = STBTTAlignedQuad.mallocStack(stack)
        return text.flatMap { char ->
          stbtt_GetBakedQuad(cdata, BITMAP_W, BITMAP_H, char.toInt() - 32, x, y, quad, true)
          listOf(
              TextVertex(quad.x0(), baseline - quad.y0(), quad.s0(), quad.t0()),
              TextVertex(quad.x1(), baseline - quad.y0(), quad.s1(), quad.t0()),
              TextVertex(quad.x1(), baseline - quad.y1(), quad.s1(), quad.t1()),
              TextVertex(quad.x0(), baseline - quad.y1(), quad.s0(), quad.t1())
          )
        }
      }
    }
  }

  fun getWidth(text: String): Int {
    return (getVertices(text).map { it.positionX }.max() ?: 0f).roundToInt()
  }

  @Suppress("UNUSED_PARAMETER")
  fun getHeight(text: String): Int {
    return ((ascent - descent + lineGap) * heightScale).roundToInt()
  }

  data class TextVertex(
      val positionX: Float,
      val positionY: Float,
      val texCoordX: Float,
      val texCoordY: Float
  )
}