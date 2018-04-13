package com.fracturedskies.render.common.shaders

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import org.lwjgl.opengl.GL42.glTexStorage3D
import org.lwjgl.stb.STBImage.stbi_load_from_memory
import java.nio.ByteBuffer

class TextureArray(width: Int, height: Int, layers: Int, rawImageBuffer: ByteBuffer?, internalFormat: Int = GL_RGBA16, format: Int = GL_RGBA, type: Int = GL_UNSIGNED_BYTE) {
  val id = glGenTextures()
  init {
    glBindTexture(GL_TEXTURE_2D_ARRAY, id)

    glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, internalFormat, width, height, layers)
    if (rawImageBuffer != null) {
      val glImageBuffer = stbi_load_from_memory(rawImageBuffer, intArrayOf(width), intArrayOf(height * layers), intArrayOf(4), 4)
      glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, 0, width, height, layers, format, type, glImageBuffer)
    } else {
      glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, 0, width, height, layers, format, type, 0)
    }

    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

    glBindTexture(GL_TEXTURE_2D_ARRAY, 0)
  }

  override fun toString() = String.format("TextureArray[$id]")
}
