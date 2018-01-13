package com.fracturedskies.game.render.components.world

import com.fracturedskies.engine.collections.ObjectMap
import com.fracturedskies.engine.math.Vector3i
import java.util.*

enum class VertexCorner(private val side1: com.fracturedskies.game.render.components.world.Occlusion, private val side2: com.fracturedskies.game.render.components.world.Occlusion, private val corner: com.fracturedskies.game.render.components.world.Occlusion) {
  TOP_LEFT(com.fracturedskies.game.render.components.world.Occlusion.TOP, com.fracturedskies.game.render.components.world.Occlusion.LEFT, com.fracturedskies.game.render.components.world.Occlusion.TOP_LEFT),
  TOP_RIGHT(com.fracturedskies.game.render.components.world.Occlusion.RIGHT, com.fracturedskies.game.render.components.world.Occlusion.TOP, com.fracturedskies.game.render.components.world.Occlusion.TOP_RIGHT),
  BOTTOM_RIGHT(com.fracturedskies.game.render.components.world.Occlusion.BOTTOM, com.fracturedskies.game.render.components.world.Occlusion.RIGHT, com.fracturedskies.game.render.components.world.Occlusion.BOTTOM_RIGHT),
  BOTTOM_LEFT(com.fracturedskies.game.render.components.world.Occlusion.LEFT, com.fracturedskies.game.render.components.world.Occlusion.BOTTOM, com.fracturedskies.game.render.components.world.Occlusion.BOTTOM_LEFT);

  private val self = com.fracturedskies.game.render.components.world.Occlusion.CENTER

  fun occlusionLevel(occlusions: EnumSet<com.fracturedskies.game.render.components.world.Occlusion>): Float {
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
  fun skyLightLevel(world: ObjectMap<com.fracturedskies.game.render.components.world.Block>, pos: Vector3i, u: Vector3i, v: Vector3i): Float {
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

  fun blockLightLevel(world: ObjectMap<com.fracturedskies.game.render.components.world.Block>, pos: Vector3i, u: Vector3i, v: Vector3i): Float {
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