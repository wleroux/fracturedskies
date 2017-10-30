package com.fracturedskies.render.mesh

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL42.*
import org.lwjgl.stb.STBImage.stbi_load_from_memory
import java.nio.ByteBuffer

class TextureArray(rawImageBuffer: ByteBuffer, width: Int, height: Int, layers: Int) {
  val id = glGenTextures()
  init {
    glBindTexture(GL_TEXTURE_2D_ARRAY, id)

    val glImageBuffer = stbi_load_from_memory(rawImageBuffer, intArrayOf(width), intArrayOf(height * layers), intArrayOf(4), 4)

    glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, GL_RGBA16, width, height, layers)
    glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, 0, width, height, layers, GL_RGBA, GL_UNSIGNED_BYTE, glImageBuffer)

    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

    glBindTexture(GL_TEXTURE_2D_ARRAY, 0)
  }
}
