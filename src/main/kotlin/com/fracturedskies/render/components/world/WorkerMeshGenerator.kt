package com.fracturedskies.render.components.world


import com.fracturedskies.engine.math.Color4
import com.fracturedskies.render.shaders.Mesh
import org.lwjgl.BufferUtils


fun generateWorkerMesh(skyLightLevel: Float, blockLightLevel: Float): () -> Mesh {
  val quads = mutableListOf<Quad>()
  // Floating Cube 0.25 -> 0.75

  // NORTH
  quads.add(Quad(
      0.25f, 0.50f, 0.00f,
      0.75f, 0.00f, -0.50f,
      0.25f, 0.00f, 0.00f,
      0f, 0f, -1f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      Color4.WHITE,
      0f, 0f, 0f, 0f
  ))

  // UP
  quads.add(Quad(
      0.25f, 0.50f, 0.00f,
      0.75f, 0.00f, 0.00f,
      0.75f, 0.00f, -0.50f,
      0f, 1f, 0f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      Color4.WHITE,
      0f, 0f, 0f, 0f
  ))

  // WEST
  quads.add(Quad(
      0.25f, 0.00f, 0.00f,
      0.75f, 0.00f, -0.50f,
      0.75f, -0.50f, 0.00f,
      -1f, 0f, 0f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      Color4.WHITE,
      0f, 0f, 0f, 0f
  ))

  // EAST
  quads.add(Quad(
      0.75f, 0.00f, 0.00f,
      0.75f, 0.00f, -0.50f,
      0.25f, 0.50f, 0.00f,
      1f, 0f, 0f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      Color4.WHITE,
      0f, 0f, 0f, 0f
  ))

  // DOWN
  quads.add(Quad(
      0.25f, 0.50f, 0.00f,
      0.25f, 0.00f, 0.00f,
      0.25f, 0.00f, 0.50f,
      0f, -1f, 0f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      Color4.WHITE,
      0f, 0f, 0f, 0f
  ))

  // SOUTH
  quads.add(Quad(
      0.75f, -0.50f, 0.00f,
      0.75f, 0.00f, -0.50f,
      0.75f, 0.00f, 0.00f,
      0f, -1f, 0f,
      skyLightLevel, skyLightLevel, skyLightLevel, skyLightLevel,
      blockLightLevel, blockLightLevel, blockLightLevel, blockLightLevel,
      Color4.WHITE,
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