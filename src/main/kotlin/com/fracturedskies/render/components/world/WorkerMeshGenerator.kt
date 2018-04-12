package com.fracturedskies.render.components.world


import com.fracturedskies.engine.math.*
import com.fracturedskies.render.shaders.Mesh
import org.lwjgl.BufferUtils


fun generateBlock(
    color: Color4,
    skyLightLevel: Float,
    blockLightLevel: Float,
    orig: Vector3,
    dimension: Vector3
): () -> Mesh {
  val quads = mutableListOf<Quad>()
  // Floating Cube 0.25 -> 0.75
  val dest = orig + dimension

  // NORTH
  quads.add(Quad(
      orig.x, dimension.x,        0.00f,
      dest.y,       0.00f, -dimension.y,
      orig.z,       0.00f,        0.00f,
      0f, 0f, -1f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      color,
      0f, 0f, 0f, 0f
  ))

  // UP
  quads.add(Quad(
      orig.x, dimension.x,        0.00f,
      dest.y,       0.00f,        0.00f,
      dest.z,       0.00f, -dimension.z,
      0f, 1f, 0f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      color,
      0f, 0f, 0f, 0f
  ))

  // WEST
  quads.add(Quad(
      orig.x,        0.00f,        0.00f,
      dest.y,        0.00f, -dimension.y,
      dest.z, -dimension.z,        0.00f,
      -1f, 0f, 0f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      color,
      0f, 0f, 0f, 0f
  ))

  // EAST
  quads.add(Quad(
      dest.x,        0.00f,        0.00f,
      dest.y,        0.00f, -dimension.y,
      orig.z,  dimension.z,        0.00f,
      1f, 0f, 0f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      color,
      0f, 0f, 0f, 0f
  ))

  // DOWN
  quads.add(Quad(
      orig.x, dimension.x,       0.00f,
      orig.y,       0.00f,       0.00f,
      orig.z,       0.00f, dimension.z,
      0f, -1f, 0f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      color,
      0f, 0f, 0f, 0f
  ))

  // SOUTH
  quads.add(Quad(
      dest.x, -dimension.x,        0.00f,
      dest.y,        0.00f, -dimension.y,
      dest.z,        0.00f,        0.00f,
      0f, -1f, 0f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      color,
      0f, 0f, 0f, 0f
  ))

  val attributeSize = Quad.Attributes.fold(0, { acc, attr -> acc + attr.elements * attr.elementSize}) / java.lang.Float.BYTES
  val verticesBuffer = BufferUtils.createFloatBuffer(attributeSize * 4 * quads.size)
  val indicesBuffer = BufferUtils.createIntBuffer(6 * quads.size)

  var vertexCount = 0
  quads.forEach {quad ->
    verticesBuffer.put(quad.vertices())
    indicesBuffer.put(quad.indices(vertexCount))
    vertexCount += 4
  }

  verticesBuffer.flip()
  val vertices = FloatArray(verticesBuffer.remaining())
  verticesBuffer.get(vertices)

  indicesBuffer.flip()
  val indices = IntArray(indicesBuffer.remaining())
  indicesBuffer.get(indices)

  return { Mesh(vertices, indices, Quad.Attributes) }
}