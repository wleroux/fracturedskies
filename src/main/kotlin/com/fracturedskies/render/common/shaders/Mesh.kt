package com.fracturedskies.render.common.shaders

import com.fracturedskies.render.world.Quad
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
class Mesh(vertices: FloatArray = FloatArray(0), indices: IntArray = IntArray(0), val attributes: List<Attribute> = emptyList()) {
  companion object {
    fun generate(quads: List<Quad>): Mesh {
      val attributeSize = Quad.Attributes.fold(0, { acc, attr -> acc + attr.elements * attr.elementSize}) / java.lang.Float.BYTES
      val verticesBuffer = BufferUtils.createFloatBuffer(attributeSize * 4 * quads.size)
      val indicesBuffer = BufferUtils.createIntBuffer(6 * quads.size)

      var indexCount = 0
      quads.forEach { quad ->
        verticesBuffer.put(quad.vertices())
        indicesBuffer.put(quad.indices(indexCount))
        indexCount += 4
      }

      verticesBuffer.flip()
      val vertices = FloatArray(verticesBuffer.remaining())
      verticesBuffer.get(vertices)

      indicesBuffer.flip()
      val indices = IntArray(indicesBuffer.remaining())
      indicesBuffer.get(indices)

      return Mesh(vertices, indices, Quad.Attributes)
    }
  }

  data class Attribute(val label: String, val location: Int, val elementType: Int, val elements: Int, val elementSize: Int) {
    companion object {
      private const val FLOAT_BYTES = 4
      private const val BYTE_BYTES = 1
      private const val POSITION_LOCATION = 0
      private const val TEXCOORD_LOCATION = 1
      private const val NORMAL_LOCATION = 2
      private const val COLOR_LOCATION = 3
      private const val OCCLUSION_LOCATION = 4
      private const val SKY_LIGHT_LEVEL_LOCATION = 5
      private const val BLOCK_LIGHT_LEVEL_LOCATION = 6

      val POSITION = Attribute("POSITION", POSITION_LOCATION, GL_FLOAT, 3, FLOAT_BYTES)
      val TEXCOORD = Attribute("TEXCOORD", TEXCOORD_LOCATION, GL_FLOAT, 3, FLOAT_BYTES)
      val NORMAL = Attribute("NORMAL", NORMAL_LOCATION, GL_FLOAT, 3, FLOAT_BYTES)
      val COLOR = Attribute("COLOR", COLOR_LOCATION, GL_UNSIGNED_BYTE, 4, BYTE_BYTES)
      val OCCLUSION = Attribute("OCCLUSION", OCCLUSION_LOCATION, GL_FLOAT, 1, FLOAT_BYTES)
      val SKY_LIGHT_LEVEL = Attribute("SKY_LIGHT_LEVEL", SKY_LIGHT_LEVEL_LOCATION, GL_FLOAT, 1, FLOAT_BYTES)
      val BLOCK_LIGHT_LEVEL = Attribute("BLOCK_LIGHT_LEVEL", BLOCK_LIGHT_LEVEL_LOCATION, GL_FLOAT, 1, FLOAT_BYTES)
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