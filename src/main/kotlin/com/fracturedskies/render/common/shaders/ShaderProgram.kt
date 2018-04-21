package com.fracturedskies.render.common.shaders

import org.lwjgl.opengl.GL11.GL_TRUE
import org.lwjgl.opengl.GL20.*

abstract class ShaderProgram(vertexShaderSource: String, fragmentShaderSource: String) {
  val id = glCreateProgram()

  init {
    val vertexShaderId = compileShader(GL_VERTEX_SHADER, vertexShaderSource)
    val fragmentShaderId = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource)
    glAttachShader(id, vertexShaderId)
    glAttachShader(id, fragmentShaderId)
    glLinkProgram(id)

    val success = glGetProgrami(id, GL_LINK_STATUS)
    if (success != GL_TRUE) {
      val errorMessage = glGetProgramInfoLog(id)
      throw RuntimeException("Could not link program: $errorMessage")
    }

    glDeleteShader(vertexShaderId)
    glDeleteShader(fragmentShaderId)
  }

  private fun compileShader(type: Int, source: String): Int {
    val shaderId = glCreateShader(type)
    glShaderSource(shaderId, source)
    glCompileShader(shaderId)

    val success = glGetShaderi(shaderId, GL_COMPILE_STATUS)
    if (success != GL_TRUE) {
      val errorMessage = glGetShaderInfoLog(shaderId)
      throw RuntimeException("Could not compile shader: $errorMessage\n$source")
    }

    return shaderId
  }

  inline fun bind(block: ()->Unit) {
    glUseProgram(id)
    block.invoke()
    glUseProgram(0)
  }

  fun close() {
    glDeleteProgram(id)
  }

  override fun toString(): String = this.javaClass.simpleName
}
