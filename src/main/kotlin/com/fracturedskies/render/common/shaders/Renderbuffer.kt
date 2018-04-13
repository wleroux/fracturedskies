package com.fracturedskies.render.common.shaders

import org.lwjgl.opengl.GL30.*

class Renderbuffer(width: Int, height: Int, format: Int) {
  val id = glGenRenderbuffers()
  init {
    glBindRenderbuffer(GL_RENDERBUFFER, id)
    glRenderbufferStorage(GL_RENDERBUFFER, format, width, height)
  }

  fun close() {
    glDeleteRenderbuffers(id)
  }
}