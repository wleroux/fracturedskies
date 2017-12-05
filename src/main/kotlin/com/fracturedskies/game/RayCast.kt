package com.fracturedskies.game

import com.fracturedskies.engine.math.Vector3
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.math.within
import kotlin.coroutines.experimental.buildSequence

fun raycast(world: World, origin: Vector3, direction: Vector3) = buildSequence {
  val normals = arrayOf(
          Vector3(-direction.x, 0f, 0f).normalize(),
          Vector3(0f, -direction.y, 0f).normalize(),
          Vector3(0f, 0f, -direction.z).normalize()
  )

  var intersection = origin.copy()
  while (true) {
    val nextPlaneX = nextPlane(intersection.x, direction.x, 0f, world.dimension.width.toFloat())
    val timeX = if (direction.x != 0f) (nextPlaneX - intersection.x) / direction.x else java.lang.Float.MAX_VALUE

    val nextPlaneY = nextPlane(intersection.y, direction.y, 0f, world.dimension.height.toFloat())
    val timeY = if (direction.y != 0f) (nextPlaneY - intersection.y) / direction.y else java.lang.Float.MAX_VALUE

    val nextPlaneZ = nextPlane(intersection.z, direction.z, 0f, world.dimension.depth.toFloat())
    val timeZ = if (direction.z != 0f) (nextPlaneZ - intersection.z) / direction.z else java.lang.Float.MAX_VALUE

    var minimumTime = Math.min(Math.min(timeX, timeY), timeZ)

    // Force planes that are out-of-bounds to intersected
    if (0f > intersection.x && direction.x > 0f || intersection.x > world.dimension.width.toFloat() && direction.x < 0f)
      minimumTime = Math.max(minimumTime, timeX)
    if (0f > intersection.y && direction.z > 0f || intersection.y > world.dimension.height.toFloat() && direction.y < 0f)
      minimumTime = Math.max(minimumTime, timeY)
    if (0f > intersection.z && direction.z > 0f || intersection.z > world.dimension.depth.toFloat() && direction.z < 0f)
      minimumTime = Math.max(minimumTime, timeZ)

    // Any further amount of time would lead us out-of-bounds
    if (minimumTime <= 0) {
      break
    }

    intersection += direction * minimumTime

    var normal = Vector3(0f, 0f, 0f)
    if (timeX == minimumTime) normal += normals[0]
    if (timeY == minimumTime) normal += normals[1]
    if (timeZ == minimumTime) normal += normals[2]
    normal = normal.normalize()

    val position = blockPosition(intersection, normal)
    if (position within world) {
      yield(BlockRaycastHit(world[position], position, direction, intersection, normal))
    }
  }
}

private fun blockPosition(intersection: Vector3, normal: Vector3) =
        Vector3i(blockCoordinate(intersection.x, normal.x), blockCoordinate(intersection.y, normal.y), blockCoordinate(intersection.z, normal.z))
private fun blockCoordinate(coordinate: Float, normal: Float): Int {
  return if (coordinate % 1 == 0f && normal > 0) {
    coordinate.toInt() - 1
  } else {
    Math.floor(coordinate.toDouble()).toInt()
  }
}

private fun nextPlane(current: Float, direction: Float, minValue: Float, maxValue: Float): Float {
  val floor = Math.floor(current.toDouble()).toFloat()
  return when {
    direction > 0f -> Math.min(floor + 1, maxValue)
    current != floor -> Math.max(minValue, Math.min(floor, maxValue))
    else -> Math.max(minValue, floor - 1)
  }
}

data class BlockRaycastHit(val block: Block, val position: Vector3i, val direction: Vector3, val intersection: Vector3, val normal: Vector3) {
  val faces: List<Vector3i> get() {
    val faces = mutableListOf<Vector3i>()
    if (this.normal.x != 0f)
      faces.add(if (this.normal.x > 0) Vector3i.AXIS_X else Vector3i.AXIS_NEG_X)
    if (this.normal.y != 0f)
      faces.add(if (this.normal.y > 0) Vector3i.AXIS_Y else Vector3i.AXIS_NEG_Y)
    if (this.normal.z != 0f)
      faces.add(if (this.normal.z > 0) Vector3i.AXIS_Z else Vector3i.AXIS_NEG_Z)
    return faces.toList()
  }
}
