package com.fracturedskies.render.colonist

import com.fracturedskies.engine.math.*
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.common.shaders.Mesh.Attribute


object ObjMeshParser {
  private val COMMENT_REGEX = Regex("^#.*$")
  private const val FLOAT_REGEX = "-?[\\d]+(?:.[\\d]+)?"
  private const val INT_REGEX = "-?[\\d]+"
  private val VERTEX_PATTERN = Regex("^v (?<x>$FLOAT_REGEX) (?<y>$FLOAT_REGEX) (?<z>$FLOAT_REGEX)$")
  private val NORMAL_PATTERN = Regex("^vn (?<dx>$FLOAT_REGEX) (?<dy>$FLOAT_REGEX) (?<dz>$FLOAT_REGEX)$")
  private val TEXTURE_PATTERN = Regex("^vt (?<u>$FLOAT_REGEX) (?<v>$FLOAT_REGEX)")
  private val FACES_PATTERN = Regex("^f ($INT_REGEX)/($INT_REGEX)/($INT_REGEX) ($INT_REGEX)/($INT_REGEX)/($INT_REGEX) ($INT_REGEX)/($INT_REGEX)/($INT_REGEX)$")
  private data class Face(val vertexIndices: List<Int>, val normalIndices: List<Int>, val textureIndices: List<Int>)

  fun generateMesh(file: String, scale: Float, colors: List<Color4>, skyLight: Float, blockLight: Float): Mesh {
    javaClass.getResourceAsStream(file).use { inputStream ->
      val lines = inputStream.bufferedReader().lineSequence()
      val verts = mutableListOf<Vector3>()
      val normals = mutableListOf<Vector3>()
      val textures = mutableListOf<Vector2>()
      val faces = mutableListOf<Face>()
      lines.forEach {
        when {
          COMMENT_REGEX.matches(it) -> Unit
          it.isBlank() -> Unit
          VERTEX_PATTERN.matches(it) -> verts += parseVertex(it)
          NORMAL_PATTERN.matches(it) -> normals += parseNormal(it)
          TEXTURE_PATTERN.matches(it) -> textures += parseTexture(it)
          FACES_PATTERN.matches(it) -> faces += parseFace(it)
          else -> Unit
        }
      }

      val vertices = FloatArray(faces.size * 3 * 10)
      val indices = IntArray(faces.size * 3)
      faces.forEachIndexed { faceIndex, face ->
        val faceVerts = face.vertexIndices.map { verts[it - 1] }
        val faceNormals = face.normalIndices.map { normals[it - 1] }
        val faceTextures = face.textureIndices.map { colors[it - 1] }

        (0 until 3).forEach { meshVertexIndex ->
          val faceVert = faceVerts[meshVertexIndex]
          val faceNormal = faceNormals[meshVertexIndex]
          val faceTexture = faceTextures[meshVertexIndex]
          vertices[faceIndex * 3 * 10 + meshVertexIndex * 10 + 0] = faceVert.x * scale
          vertices[faceIndex * 3 * 10 + meshVertexIndex * 10 + 1] = faceVert.y * scale
          vertices[faceIndex * 3 * 10 + meshVertexIndex * 10 + 2] = faceVert.z * scale
          vertices[faceIndex * 3 * 10 + meshVertexIndex * 10 + 3] = faceTexture.toFloat()
          vertices[faceIndex * 3 * 10 + meshVertexIndex * 10 + 4] = skyLight
          vertices[faceIndex * 3 * 10 + meshVertexIndex * 10 + 5] = blockLight
          vertices[faceIndex * 3 * 10 + meshVertexIndex * 10 + 6] = 0f
          vertices[faceIndex * 3 * 10 + meshVertexIndex * 10 + 7] = faceNormal.x
          vertices[faceIndex * 3 * 10 + meshVertexIndex * 10 + 8] = faceNormal.y
          vertices[faceIndex * 3 * 10 + meshVertexIndex * 10 + 9] = faceNormal.z
        }

        indices[faceIndex * 3 + 0] = faceIndex * 3 + 2
        indices[faceIndex * 3 + 1] = faceIndex * 3 + 1
        indices[faceIndex * 3 + 2] = faceIndex * 3 + 0
      }

      return Mesh(vertices, indices, listOf(
          Attribute.POSITION,
          Attribute.COLOR,
          Attribute.SKY_LIGHT_LEVEL,
          Attribute.BLOCK_LIGHT_LEVEL,
          Attribute.OCCLUSION,
          Attribute.NORMAL
      ))
    }
  }

  private fun parseFace(line: String): Face {
    val values = FACES_PATTERN.find(line)!!.groupValues.drop(1).map(String::toInt)
    val vertexIndices = listOf(values[0], values[3], values[6])
    val textureIndices = listOf(values[1], values[4], values[7])
    val normalIndices = listOf(values[2], values[5], values[8])
    return Face(vertexIndices, normalIndices, textureIndices)
  }

  private fun parseTexture(line: String): Vector2 {
    val values = TEXTURE_PATTERN.find(line)!!.groups
    return Vector2(
        values["u"]!!.value.toFloat(),
        values["v"]!!.value.toFloat()
    )
  }

  private fun parseNormal(line: String): Vector3 {
    val values = NORMAL_PATTERN.find(line)!!.groups
    return Vector3(
        values["dx"]!!.value.toFloat(),
        values["dy"]!!.value.toFloat(),
        values["dz"]!!.value.toFloat()
    )
  }

  private fun parseVertex(line: String): Vector3 {
    val values = VERTEX_PATTERN.find(line)!!.groups
    return Vector3(
        values["x"]!!.value.toFloat(),
        values["y"]!!.value.toFloat(),
        values["z"]!!.value.toFloat()
    )
  }
}