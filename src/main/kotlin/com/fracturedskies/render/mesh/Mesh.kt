package com.fracturedskies.render.mesh

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

class Mesh(vertices: FloatArray, indices: IntArray, vararg attributes: Attribute) {
  data class Attribute(val location: Int, val elementType: Int, val elements: Int, val elementSize: Int) {
    /**
     * Standard Vertex Attributes
     */
    companion object {
      private val POSITION_LOCATION = 0
      private val TEXCOORD_LOCATION = 1
      private val NORMAL_LOCATION = 2

      val POSITION = Mesh.Attribute(POSITION_LOCATION, GL11.GL_FLOAT, 3, java.lang.Float.BYTES)
      val TEXCOORD = Mesh.Attribute(TEXCOORD_LOCATION, GL11.GL_FLOAT, 3, java.lang.Float.BYTES)
      val NORMAL = Mesh.Attribute(NORMAL_LOCATION, GL11.GL_FLOAT, 3, java.lang.Float.BYTES)
    }
  }

  val vao = glGenVertexArrays()
  val indexCount = indices.size

  init {
    glBindVertexArray(vao)

    val vbo = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    val stride = attributes.fold(0, { acc, attr ->
      acc + attr.elements * attr.elementSize
    })
    var offset = 0
    for ((location, elementType, elements, elementSize) in attributes) {
      glVertexAttribPointer(location, elements, elementType, false, stride, offset.toLong())
      glEnableVertexAttribArray(location)
      offset += elements * elementSize
    }

    val ebo = glGenBuffers()
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

    glBindVertexArray(0)
  }
}