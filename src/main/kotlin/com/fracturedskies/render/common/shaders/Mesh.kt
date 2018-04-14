package com.fracturedskies.render.common.shaders

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import java.lang.Byte
import java.lang.Float
data class Mesh(val vertices: FloatArray = FloatArray(0), val indices: IntArray = IntArray(0), val attributes: List<Attribute> = emptyList()) {
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
      private val SKY_LIGHT_LEVEL_LOCATION = 5
      private val BLOCK_LIGHT_LEVEL_LOCATION = 6

      val POSITION = Attribute("POSITION", POSITION_LOCATION, GL_FLOAT, 3, Float.BYTES)
      val TEXCOORD = Attribute("TEXCOORD", TEXCOORD_LOCATION, GL_FLOAT, 3, Float.BYTES)
      val NORMAL = Attribute("NORMAL", NORMAL_LOCATION, GL_FLOAT, 3, Float.BYTES)
      val COLOR = Attribute("COLOR", COLOR_LOCATION, GL_UNSIGNED_BYTE, 4, Byte.BYTES)
      val OCCLUSION = Attribute("OCCLUSION", OCCLUSION_LOCATION, GL_FLOAT, 1, Float.BYTES)
      val SKY_LIGHT_LEVEL = Attribute("SKY_LIGHT_LEVEL", SKY_LIGHT_LEVEL_LOCATION, GL_FLOAT, 1, Float.BYTES)
      val BLOCK_LIGHT_LEVEL = Attribute("BLOCK_LIGHT_LEVEL", BLOCK_LIGHT_LEVEL_LOCATION, GL_FLOAT, 1, Float.BYTES)
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