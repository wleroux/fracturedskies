package com.fracturedskies.game.render.shaders

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer

class Texture(width: Int, height: Int, rawImageBuffer: ByteBuffer? = null, internalFormat: Int = GL_RGBA16, format: Int = GL_RGBA, type: Int = GL_UNSIGNED_BYTE ) {
  val id = glGenTextures()
  init {
    glBindTexture(GL_TEXTURE_2D, id)

    if (rawImageBuffer != null) {
      val glImageBuffer = STBImage.stbi_load_from_memory(rawImageBuffer, intArrayOf(width), intArrayOf(height), intArrayOf(4), 4)
      glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, glImageBuffer)
    } else {
      glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, 0)
    }

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

    glBindTexture(GL_TEXTURE_2D, 0)
  }

  fun close() {
    glDeleteTextures(id)
  }

  override fun toString() = String.format("Texture[$id]")
}
