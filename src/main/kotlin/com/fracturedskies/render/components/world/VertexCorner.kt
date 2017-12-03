package com.fracturedskies.render.components.world

import java.util.*

enum class VertexCorner(private val side1: Occlusion, private val side2: Occlusion, private val corner: Occlusion) {
  TOP_LEFT(Occlusion.TOP, Occlusion.LEFT, Occlusion.TOP_LEFT),
  TOP_RIGHT(Occlusion.RIGHT, Occlusion.TOP, Occlusion.TOP_RIGHT),
  BOTTOM_RIGHT(Occlusion.BOTTOM, Occlusion.RIGHT, Occlusion.BOTTOM_RIGHT),
  BOTTOM_LEFT(Occlusion.LEFT, Occlusion.BOTTOM, Occlusion.BOTTOM_LEFT);

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
}