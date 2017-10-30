package com.fracturedskies.render

import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.render.mesh.Material
import com.fracturedskies.render.mesh.Mesh
import com.fracturedskies.render.mesh.TextureArray
import com.fracturedskies.render.mesh.standard.StandardShaderProgram

/**
 * A block
 */
class Block {
  private val material = Material(StandardShaderProgram(), StandardShaderProgram.Properties(TextureArray(loadByteBuffer("com/fracturedskies/render/tileset.png", this.javaClass.classLoader), 16, 16, 3)))
  private val mesh = Mesh(floatArrayOf(
          -0.5f, -0.5f, 0f,   0f, 0f, 1f,   0f, 0f, 1f,
          -0.5f,  0.5f, 0f,   0f, 1f, 1f,   0f, 0f, 1f,
          0.5f,  0.5f, 0f,   1f, 1f, 1f,   0f, 0f, 1f,
          0.5f, -0.5f, 0f,   1f, 0f, 1f,   0f, 0f, 1f
  ), intArrayOf(
          0, 1, 2,
          2, 3, 0
  ), Mesh.Attribute.POSITION, Mesh.Attribute.TEXCOORD, Mesh.Attribute.NORMAL)

  fun render(variables: StandardShaderProgram.Variables) {
    material.render(variables, mesh)
  }
}