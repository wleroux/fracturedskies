package com.fracturedskies.render.world

import com.fracturedskies.engine.collections.Space
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.render.world.Occlusion.*
import com.fracturedskies.api.block.Block
import java.util.*

enum class VertexCorner(private val side1: Occlusion, private val side2: Occlusion, private val corner: Occlusion) {
  TOP_LEFT(TOP, LEFT, Occlusion.TOP_LEFT),
  TOP_RIGHT(RIGHT, TOP, Occlusion.TOP_RIGHT),
  BOTTOM_RIGHT(BOTTOM, RIGHT, Occlusion.BOTTOM_RIGHT),
  BOTTOM_LEFT(LEFT, BOTTOM, Occlusion.BOTTOM_LEFT);

  private val self = CENTER

  fun occlusionLevel(occlusions: EnumSet<Occlusion>): Float {
    val side1 = occlusions.contains(side1)
    val side2 = occlusions.contains(side2)
    val corner = occlusions.contains(corner)
    return when {
      side1 && side2 -> 0f/3f
      corner && (side1 or side2) -> 1f/3f
      corner or side1 or side2 -> 2f/3f
      else -> 3f/3f
    }
  }
  fun skyLightLevel(world: Space<Block>, pos: Vector3i, u: Vector3i, v: Vector3i): Float {
    var sides = arrayOf(side1, corner, side2, self)
        .filter { it.gathersLight(world, pos, u, v) }
    if (sides.contains(corner) && !sides.contains(side1) && !sides.contains(side2))
      sides -= corner
    val skyLightValues = sides.map { it.skyLight(world, pos, u, v) }
    return if (skyLightValues.isEmpty()) {
      0f
    } else {
      skyLightValues.fold(0, { acc, value -> acc + value}).toFloat() / skyLightValues.size.toFloat()
    }
  }

  fun blockLightLevel(world: Space<Block>, pos: Vector3i, u: Vector3i, v: Vector3i): Float {
    var sides = arrayOf(side1, corner, side2, self)
        .filter { it.gathersLight(world, pos, u, v) }
    if (sides.contains(corner) && !sides.contains(side1) && !sides.contains(side2))
      sides -= corner
    val blockLightValues = sides.map { it.blockLight(world, pos, u, v) }
    return if (blockLightValues.isEmpty()) {
      0f
    } else {
      blockLightValues.fold(0, { acc, value -> acc + value }).toFloat() / blockLightValues.size.toFloat()
    }
  }
}