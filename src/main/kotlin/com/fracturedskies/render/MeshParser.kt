package com.fracturedskies.render

import com.fracturedskies.api.block.*
import com.fracturedskies.api.block.data.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.Vector3i.Companion.area
import com.fracturedskies.render.world.Quad


object MeshParser {
  private val COMMENT_REGEX = Regex("^#.*$")
  private const val INT_REGEX = "-?[\\d]+"
  private const val STRING_REGEX = "[^\\s]+"
  private val DIMENSION_PATTERN = Regex("^dimension +(?<width>$INT_REGEX) +(?<height>$INT_REGEX) +(?<depth>$INT_REGEX)$")
  private val COLOR_PATTERN = Regex("^color +(?<label>$STRING_REGEX) +(?<red>$INT_REGEX) +(?<green>$INT_REGEX) +(?<blue>$INT_REGEX)$")
  private val FILL_PATTERN = Regex("^fill +(?<sx>$INT_REGEX) +(?<sy>$INT_REGEX) +(?<sz>$INT_REGEX) +(?<tx>$INT_REGEX) +(?<ty>$INT_REGEX) +(?<tz>$INT_REGEX) +(?<label>$STRING_REGEX)$")
  fun generateQuads(file: String, skyLight: Int, blockLight: Int, colorOverrides: Map<String, Color4>, offset: Vector3, scale: Float): Sequence<Quad> {
    javaClass.getResourceAsStream(file).use { inputStream ->
      // Construct Space
      lateinit var space: ObjectMutableSpace<Block>
      val colors = colorOverrides.toMutableMap()
      val colorBlocks = mutableMapOf<String, Block>()

      val lines = inputStream.bufferedReader().lineSequence()
      lines.forEach { line ->
        when {
          DIMENSION_PATTERN.matches(line) -> {
            space = parseDimension(line, skyLight, blockLight)
          }
          COLOR_PATTERN.matches(line) -> {
            val (label, color) = parseColor(line)
            colors.putIfAbsent(label, color)
          }
          FILL_PATTERN.matches(line) -> {
            val (label, area) = parseFill(line)
            val blockColor = colorBlocks.computeIfAbsent(label, {
              Block(ColorBlockType(colors[label]!!)).with(SkyLight(skyLight)).with(BlockLight(blockLight))
            })
            area.forEach { space[it] = blockColor }
          }
        }
      }

      // Generate Mesh
      return space.flatMap { (pos, block) ->
        block.type.model.quads(space, pos, false, offset, scale)
      }
    }
  }

  private fun parseDimension(line: String, skyLight: Int, blockLight: Int): ObjectMutableSpace<Block> {
    val values = DIMENSION_PATTERN.find(line)!!.groups
    val width = values["width"]!!.value.toInt()
    val height = values["height"]!!.value.toInt()
    val depth = values["depth"]!!.value.toInt()
    val blockAir = Block(BlockTypeAir).with(SkyLight(skyLight)).with(BlockLight(blockLight))
    return ObjectMutableSpace(Dimension(width, height, depth), {blockAir})
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
}