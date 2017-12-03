package com.fracturedskies.render.shaders

import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*

data class Mesh(val vertices: FloatArray, val indices: IntArray, val attributes: List<Attribute>) {
  data class Attribute(val label: String, val location: Int, val elementType: Int, val elements: Int, val elementSize: Int) {
    /**
     * Standard Vertex Attributes
     */
    companion object {
      private val POSITION_LOCATION = 0
      private val TEXCOORD_LOCATION = 1
      private val NORMAL_LOCATION = 2
      private val COLOR_LOCATION = 3
      private val OCCLUSION_LOCATION = 4

      val POSITION = Mesh.Attribute("POSITION", POSITION_LOCATION, GL_FLOAT, 3, java.lang.Float.BYTES)
      val TEXCOORD = Mesh.Attribute("TEXCOORD", TEXCOORD_LOCATION, GL_FLOAT, 3, java.lang.Float.BYTES)
      val NORMAL = Mesh.Attribute("NORMAL", NORMAL_LOCATION, GL_FLOAT, 3, java.lang.Float.BYTES)
      val COLOR = Mesh.Attribute("COLOR", COLOR_LOCATION, GL_UNSIGNED_BYTE, 4, java.lang.Byte.BYTES)
      val OCCLUSION = Mesh.Attribute("OCCLUSION", OCCLUSION_LOCATION, GL_FLOAT, 1, java.lang.Float.BYTES)
    }

    override fun toString(): String = label
  }

  val vao = glGenVertexArrays()
  private val vbo: Int
  private val ebo: Int
  val indexCount = indices.size

  init {
    glBindVertexArray(vao)

    vbo = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    val stride = attributes.fold(0, { acc, attr ->
      acc + attr.elements * attr.elementSize
    })
    var offset = 0
    for ((_, location, elementType, elements, elementSize) in attributes) {
      glVertexAttribPointer(location, elements, elementType, false, stride, offset.toLong())
      glEnableVertexAttribArray(location)
      offset += elements * elementSize
    }

    ebo = glGenBuffers()
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

    glBindVertexArray(0)
  }

  fun close() {
    glDeleteBuffers(vbo)
    glDeleteBuffers(ebo)
    glDeleteVertexArrays(vao)
  }
}