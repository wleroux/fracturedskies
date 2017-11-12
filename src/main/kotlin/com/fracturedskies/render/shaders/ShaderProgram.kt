package com.fracturedskies.render.shaders

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.math.Matrix4
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30.glBindVertexArray

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
      throw RuntimeException("Could not link program: " + errorMessage)
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
      throw RuntimeException("Could not compile shader: " + errorMessage + "\n" + source)
    }

    return shaderId
  }

  protected fun uniform(location: Int, mat4: Matrix4) {
    val mat4Buffer = BufferUtils.createFloatBuffer(16)
    mat4.store(mat4Buffer)
    mat4Buffer.flip()
    glUniformMatrix4fv(location, false, mat4Buffer)
  }

  protected fun uniform(location: Int, textureUnit: Int, textureArray: TextureArray) {
    glUniform1i(location, textureUnit - GL_TEXTURE0)
    glActiveTexture(textureUnit)
    glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, textureArray.id)
  }

  protected fun uniform(location: Int, textureUnit: Int, texture: Texture) {
    glUniform1i(location, textureUnit - GL_TEXTURE0)
    glActiveTexture(textureUnit)
    glBindTexture(GL_TEXTURE_2D, texture.id)
  }

  protected fun draw(mesh: Mesh) {
    glBindVertexArray(mesh.vao)
    glDrawElements(GL_TRIANGLES, mesh.indexCount, GL_UNSIGNED_INT, 0)
  }

  fun close() {
    glDeleteProgram(id)
  }

  override fun toString(): String = this.javaClass.simpleName

  abstract fun render(properties: Context, variables: Context, mesh: Mesh)
}
