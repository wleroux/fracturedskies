package com.fracturedskies.render.components.world

import com.fracturedskies.engine.collections.ObjectMap
import com.fracturedskies.engine.math.Vector3i
import java.util.*

enum class Occlusion(private val offset: (Vector3i, Vector3i, Vector3i) -> Vector3i) {
  TOP_LEFT({pos, u, v -> (pos - v - u)}),
  TOP({pos, _, v -> (pos - v)}),
  TOP_RIGHT({pos, u, v -> (pos - v + u)}),
  LEFT({pos, u, _ -> (pos - u)}),
  CENTER({pos, _, _ -> pos}),
  RIGHT({pos, u, _ -> (pos + u)}),
  BOTTOM_LEFT({pos, u, v -> (pos + v - u)}),
  BOTTOM({pos, _, v -> (pos + v)}),
  BOTTOM_RIGHT({pos, u, v -> (pos + v + u)});
  fun gathersLight(world: ObjectMap<Block>, pos: Vector3i, u: Vector3i, v: Vector3i): Boolean {
    val target = offset(pos, u, v)
    return world.has(target) && !world[target].type.opaque
  }
  fun opaque(world: ObjectMap<Block>, pos: Vector3i, u: Vector3i, v: Vector3i): Boolean {
    val target = offset(pos, u, v)
    return if (world.has(target)) world[target].type.opaque else false
  }
  fun skyLight(world: ObjectMap<Block>, pos: Vector3i, u: Vector3i, v: Vector3i): Int {
    val target = offset(pos, u, v)
    return if (world.has(target)) world[target].skyLight else 0
  }
  fun blockLight(world: ObjectMap<Block>, pos: Vector3i, u: Vector3i, v: Vector3i): Int {
    val target = offset(pos, u, v)
    return if (world.has(target)) world[target].blockLight else 0
  }

  companion object {
    fun of(world: ObjectMap<Block>, pos: Vector3i, u: Vector3i, v: Vector3i): EnumSet<Occlusion> {
      val occlusions = EnumSet.noneOf(Occlusion::class.java)
      occlusions.addAll(Occlusion.values().filter({ it.opaque(world, pos, u, v) }))
      return occlusions
    }

    val none = EnumSet.noneOf(Occlusion::class.java)
    val all = EnumSet.allOf(Occlusion::class.java)
  }
}