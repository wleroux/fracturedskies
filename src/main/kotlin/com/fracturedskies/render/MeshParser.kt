package com.fracturedskies.render

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_NEG_X
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_NEG_Y
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_NEG_Z
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_X
import com.fracturedskies.engine.math.Vector3i.Companion.AXIS_Z
import com.fracturedskies.engine.math.Vector3i.Companion.area
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.world.*
import com.fracturedskies.render.world.VertexCorner.*
import org.lwjgl.BufferUtils


object MeshParser {
  private val COMMENT_REGEX = Regex("^#.*$")
  private const val INT_REGEX = "-?[\\d]+"
  private const val STRING_REGEX = "[^\\s]+"
  private val DIMENSION_PATTERN = Regex("^dimension +(?<width>$INT_REGEX) +(?<height>$INT_REGEX) +(?<depth>$INT_REGEX)$")
  private val COLOR_PATTERN = Regex("^color +(?<label>$STRING_REGEX) +(?<red>$INT_REGEX) +(?<green>$INT_REGEX) +(?<blue>$INT_REGEX)$")
  private val FILL_PATTERN = Regex("^fill +(?<sx>$INT_REGEX) +(?<sy>$INT_REGEX) +(?<sz>$INT_REGEX) +(?<tx>$INT_REGEX) +(?<ty>$INT_REGEX) +(?<tz>$INT_REGEX) +(?<label>$STRING_REGEX)$")
  fun generateMesh(file: String, scale: Float, skyLight: Int, blockLight: Int, colorOverrides: Map<String, Color4>): Mesh {
    javaClass.getResourceAsStream(file).use { inputStream ->
      // Construct Space
      lateinit var space: ObjectMutableSpace<String?>
      val colors = colorOverrides.toMutableMap()
      val lines = inputStream.bufferedReader().lineSequence()
      lines.forEach { line ->
        when {
          DIMENSION_PATTERN.matches(line) -> {
            space = parseDimension(line)
          }
          COLOR_PATTERN.matches(line) -> {
            val (label, color) = parseColor(line)
            colors.putIfAbsent(label, color)
          }
          FILL_PATTERN.matches(line) -> {
            val (label, area) = parseFill(line)
            area.forEach { space[it] = label }
          }
        }
      }

      // Generate Mesh
      return generateMesh(space, scale, colors, skyLight.toFloat(), blockLight.toFloat())
    }
  }

  private fun parseDimension(line: String): ObjectMutableSpace<String?> {
    val values = DIMENSION_PATTERN.find(line)!!.groups
    val width = values["width"]!!.value.toInt()
    val height = values["height"]!!.value.toInt()
    val depth = values["depth"]!!.value.toInt()
    return ObjectMutableSpace(Dimension(width, height, depth), {null})
  }

  private fun parseColor(line: String): Pair<String, Color4> {
    val values = COLOR_PATTERN.find(line)!!.groups
    val label = values["label"]!!.value
    val red = values["red"]!!.value.toInt()
    val green = values["green"]!!.value.toInt()
    val blue = values["blue"]!!.value.toInt()

    return label to Color4(red, green, blue, 255)
  }

  private fun parseFill(line: String): Pair<String, Sequence<Vector3i>> {
    val values = FILL_PATTERN.find(line)!!.groups
    val label = values["label"]!!.value
    val sx = values["sx"]!!.value.toInt()
    val sy = values["sy"]!!.value.toInt()
    val sz = values["sz"]!!.value.toInt()
    val tx = values["tx"]!!.value.toInt()
    val ty = values["ty"]!!.value.toInt()
    val tz = values["tz"]!!.value.toInt()

    return label to area(sx..tx, sy..ty, sz..tz)
  }



  private fun generateMesh(
      space: Space<String?>,
      scale: Float,
      colors: Map<String, Color4>,
      skyLight: Float,
      blockLight: Float
  ): Mesh {
    val quads = mutableListOf<Quad>()
    for (y in 0 until space.height) {
      for (x in 0 until space.width) {
        for (z in 0 until space.depth) {
          val pos = Vector3i(x, y, z)
          val label = space[x, y, z] ?: continue
          val color = colors[label] ?: continue

          val xOffset = x.toFloat() - (space.width / 2)
          val yOffset = y.toFloat()
          val zOffset = z.toFloat() - (space.depth / 2)

          // north
          val northPos =  pos + Vector3i.AXIS_NEG_Z
          if (isEmpty(space, northPos)) {
            val occlusions = Occlusion.of(space.project { it != null }, northPos, AXIS_X, AXIS_NEG_Y)
            quads.add(Quad(
                (xOffset + 0f) * scale, 1f * scale,  0f * scale,
                (yOffset + 1f) * scale, 0f * scale, -1f * scale,
                (zOffset + 0f) * scale, 0f * scale,  0f * scale,
                0f, 0f, -1f,
                skyLight, skyLight, skyLight, skyLight,
                blockLight, blockLight, blockLight, blockLight,
                color,
                TOP_LEFT.occlusionLevel(occlusions),
                TOP_RIGHT.occlusionLevel(occlusions),
                BOTTOM_RIGHT.occlusionLevel(occlusions),
                BOTTOM_LEFT.occlusionLevel(occlusions)
            ))
          }

          // up
          val upPos = pos + Vector3i.AXIS_Y
          if (isEmpty(space, upPos)) {
            val occlusions = Occlusion.of(space.project { it != null }, upPos, AXIS_X, AXIS_NEG_Z)
            quads.add(Quad(
                (xOffset + 0f) * scale, 1f * scale, 0f * scale,
                (yOffset + 1f) * scale, 0f * scale, 0f * scale,
                (zOffset + 1f) * scale, 0f * scale, -1f * scale,
                0f, 1f, 0f,
                skyLight, skyLight, skyLight, skyLight,
                blockLight, blockLight, blockLight, blockLight,
                color,
                TOP_LEFT.occlusionLevel(occlusions),
                TOP_RIGHT.occlusionLevel(occlusions),
                BOTTOM_RIGHT.occlusionLevel(occlusions),
                BOTTOM_LEFT.occlusionLevel(occlusions)
            ))
          }

          // west
          val westPos = pos + Vector3i.AXIS_NEG_X
          if (isEmpty(space, westPos)) {
            val occlusions = Occlusion.of(space.project { it != null }, westPos, AXIS_NEG_Z, AXIS_NEG_Y)
            quads.add(Quad(
                (xOffset + 0f) * scale,  0f * scale,  0f * scale,
                (yOffset + 1f) * scale,  0f * scale, -1f * scale,
                (zOffset + 1f) * scale, -1f * scale,  0f * scale,
                -1f, 0f, 0f,
                skyLight, skyLight, skyLight, skyLight,
                blockLight, blockLight, blockLight, blockLight,
                color,
                TOP_LEFT.occlusionLevel(occlusions),
                TOP_RIGHT.occlusionLevel(occlusions),
                BOTTOM_RIGHT.occlusionLevel(occlusions),
                BOTTOM_LEFT.occlusionLevel(occlusions)
            ))
          }

          // east
          val eastPos = pos + Vector3i.AXIS_X
          if (isEmpty(space, eastPos)) {
            val occlusions = Occlusion.of(space.project { it != null }, eastPos, AXIS_Z, AXIS_NEG_Y)
            quads.add(Quad(
                (xOffset + 1f) * scale, 0f * scale,  0f * scale,
                (yOffset + 1f) * scale, 0f * scale, -1f * scale,
                (zOffset + 0f) * scale, 1f * scale,  0f * scale,
                1f, 0f, 0f,
                skyLight, skyLight, skyLight, skyLight,
                blockLight, blockLight, blockLight, blockLight,
                color,
                TOP_LEFT.occlusionLevel(occlusions),
                TOP_RIGHT.occlusionLevel(occlusions),
                BOTTOM_RIGHT.occlusionLevel(occlusions),
                BOTTOM_LEFT.occlusionLevel(occlusions)
            ))
          }

          // down
          val downPos = pos + Vector3i.AXIS_NEG_Y
          if (isEmpty(space, downPos)) {
            val occlusions = Occlusion.of(space.project { it != null }, downPos, AXIS_X, AXIS_Z)
            quads.add(Quad(
                (xOffset + 0f) * scale, 1f * scale, 0f * scale,
                (yOffset + 0f) * scale, 0f * scale, 0f * scale,
                (zOffset + 0f) * scale, 0f * scale, 1f * scale,
                0f, -1f, 0f,
                skyLight, skyLight, skyLight, skyLight,
                blockLight, blockLight, blockLight, blockLight,
                color,
                TOP_LEFT.occlusionLevel(occlusions),
                TOP_RIGHT.occlusionLevel(occlusions),
                BOTTOM_RIGHT.occlusionLevel(occlusions),
                BOTTOM_LEFT.occlusionLevel(occlusions)
            ))
          }

          // south
          val southPos = pos + Vector3i.AXIS_Z
          if (isEmpty(space, southPos)) {
            val occlusions = Occlusion.of(space.project { it != null }, southPos, AXIS_NEG_X, AXIS_NEG_Y)
            quads.add(Quad(
                (xOffset + 1f) * scale, -1f * scale,  0f * scale,
                (yOffset + 1f) * scale,  0f * scale, -1f * scale,
                (zOffset + 1f) * scale,  0f * scale,  0f * scale,
                0f, 0f, 1f,
                skyLight, skyLight, skyLight, skyLight,
                blockLight, blockLight, blockLight, blockLight,
                color,
                TOP_LEFT.occlusionLevel(occlusions),
                TOP_RIGHT.occlusionLevel(occlusions),
                BOTTOM_RIGHT.occlusionLevel(occlusions),
                BOTTOM_LEFT.occlusionLevel(occlusions)
            ))
          }
        }
      }
    }

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

    return Mesh(vertices, indices, Quad.Attributes)
  }

  private fun isEmpty(space: Space<String?>, pos: Vector3i): Boolean {
    return if (!space.has(pos)) true else space[pos] === null
  }
}