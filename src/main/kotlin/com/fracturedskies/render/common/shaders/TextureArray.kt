package com.fracturedskies.render.common.shaders

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.*
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import org.lwjgl.opengl.GL42.glTexStorage3D
import java.nio.ByteBuffer

class TextureArray(width: Int, height: Int, depth: Int, pixels: ByteBuffer, internalFormat: Int = GL_RGBA16, format: Int = GL_RGBA, type: Int = GL_UNSIGNED_BYTE) {
  val id = glGenTextures()
  init {
    glBindTexture(GL_TEXTURE_2D_ARRAY, id)

    glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, internalFormat, width, height, depth)
    glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, 0, width, height, depth, format, type, pixels)
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

    glBindTexture(GL_TEXTURE_2D_ARRAY, 0)
  }

  fun close() {
    glDeleteTextures(id)
  }

  override fun toString() = String.format("TextureArray[$id]")
}
