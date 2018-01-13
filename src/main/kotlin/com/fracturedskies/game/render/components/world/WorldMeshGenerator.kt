package com.fracturedskies.game.render.components.world

import com.fracturedskies.engine.collections.ObjectMap
import com.fracturedskies.engine.math.*
import com.fracturedskies.game.render.shaders.Mesh
import org.lwjgl.BufferUtils
import java.util.*

/**
 * A greedy mesh implementation
 */
fun generateWorldMesh(
    world: ObjectMap<com.fracturedskies.game.render.components.world.Block>,
    sliceMesh: Boolean,
    xRange: IntRange, yRange: IntRange, zRange: IntRange
): () -> Mesh {
  val ranges = arrayOf(xRange, yRange, zRange)
  val dimensions = arrayOf(xRange.count(), yRange.count(), zRange.count())
  val vectors = arrayOf(Vector3i.AXIS_X, Vector3i.AXIS_Y, Vector3i.AXIS_Z)

  //Sweep over 3-axes
  val quads = mutableListOf<com.fracturedskies.game.render.components.world.Quad>()
  for (d in 0..2) {
    val u = (d + 1) % 3
    val v = (d + 2) % 3
    val mask = arrayOfNulls<Data>(dimensions[u] * dimensions[v])

    val pos = intArrayOf(0, 0, 0)
    pos[d] = if (ranges[d].start == 0) ranges[d].start - 1 else ranges[d].start
    while (pos[d] <= ranges[d].endInclusive) {
      //Compute mask
      var n = 0
      ranges[v].forEach { xv ->
        pos[v] = xv
        ranges[u].forEach { xu ->
          pos[u] = xu
          mask[n] = getData(world, sliceMesh, Vector3i(pos[0], pos[1], pos[2]), vectors[d], vectors[u], vectors[v])
          n ++
        }
      }

      //Increment x[d]
      ++pos[d]

      //Generate mesh for mask using lexicographic ordering
      n = 0
      pos[v] = ranges[v].start
      while (ranges[v].contains(pos[v])) {
        pos[u] = ranges[u].start
        while (ranges[u].contains(pos[u])) {
          var width = 1
          var height = 1

          val data = mask[n]
          if (data != null) {
            // Get width
            while (ranges[u].contains(pos[u] + width) && data == mask[n + width]) {
              width++
            }

            // Get height
            findHeight@ while (ranges[v].contains(pos[v] + height)) {
              for (k in 0 until width) {
                if (data != mask[n + k + height * dimensions[u]]) {
                  break@findHeight
                }
              }
              height++
            }

            // Add quad
            val du = intArrayOf(0, 0, 0)
            du[u] = width
            val dv = intArrayOf(0, 0, 0)
            dv[v] = height
            var quad = com.fracturedskies.game.render.components.world.Quad(pos, du, dv, vectors[d], data.skyLight, data.blockLight, data.color, data.occlusion)
            if (data.reversed)
              quad = quad.reverse()
            if (quad.topLeftOcclusion + quad.bottomRightOcclusion < quad.topRightOcclusion + quad.bottomLeftOcclusion)
              quad = quad.rotate()
            quads.add(quad)

            // Clear quad's mask
            for (i in 0 until height) {
              for (j in 0 until width) {
                mask[n + j + i * dimensions[u]] = null
              }
            }
          }

          //Increment counters and continue
          pos[u] += width
          n += width
        }
        pos[v]++
      }
    }
  }

  // Generate Mesh
  val attributeSize = com.fracturedskies.game.render.components.world.Quad.Attributes.fold(0, { acc, attr -> acc + attr.elements * attr.elementSize}) / java.lang.Float.BYTES
  val verticesBuffer = BufferUtils.createFloatBuffer(attributeSize * 4 * quads.size)
  val indicesBuffer = BufferUtils.createIntBuffer(6 * quads.size)

  var indexCount = 0
  quads.forEach { quad ->
    verticesBuffer.put(quad.vertices())
    indicesBuffer.put(quad.indices(indexCount))
    indexCount += 4
  }

  verticesBuffer.flip()
  val vertices = FloatArray(verticesBuffer.remaining())
  verticesBuffer.get(vertices)

  indicesBuffer.flip()
  val indices = IntArray(indicesBuffer.remaining())
  indicesBuffer.get(indices)

  return { Mesh(vertices, indices, com.fracturedskies.game.render.components.world.Quad.Attributes) }
}


private fun getBlock(world: ObjectMap<com.fracturedskies.game.render.components.world.Block>, pos: Vector3i): com.fracturedskies.game.render.components.world.Block? {
  return if (world.has(pos)) { world[pos] } else { null }
}
private fun isOpaque(block: com.fracturedskies.game.render.components.world.Block?) = block?.type?.opaque ?: false

private data class Data(
    val color: Color4,
    val skyLight: Map<com.fracturedskies.game.render.components.world.VertexCorner, Float>,
    val blockLight: Map<com.fracturedskies.game.render.components.world.VertexCorner, Float>,
    val reversed: Boolean,
    val occlusion: EnumSet<com.fracturedskies.game.render.components.world.Occlusion>
)

private fun getData(world: ObjectMap<com.fracturedskies.game.render.components.world.Block>, sliceMesh: Boolean, pos: Vector3i, d: Vector3i, u: Vector3i, v: Vector3i): Data? {
  val currentBlock = getBlock(world, pos)
  val nextBlock = getBlock(world, pos + d)

  return when {
    !sliceMesh && isOpaque(currentBlock) && !isOpaque(nextBlock) -> {
      val skyLightLevels = com.fracturedskies.game.render.components.world.VertexCorner.values().associate { it to it.skyLightLevel(world, pos + d, u, v) }
      val blockLightLevels = com.fracturedskies.game.render.components.world.VertexCorner.values().associate { it to it.blockLightLevel(world, pos + d, u, v) }

      Data(currentBlock!!.type.color, skyLightLevels, blockLightLevels, false, com.fracturedskies.game.render.components.world.Occlusion.of(world, pos + d, u, v))
    }
    !sliceMesh && isOpaque(nextBlock) && !isOpaque(currentBlock) -> {
      val skyLightLevels = com.fracturedskies.game.render.components.world.VertexCorner.values().associate { it to it.skyLightLevel(world, pos, u, v) }
      val blockLightLevels = com.fracturedskies.game.render.components.world.VertexCorner.values().associate { it to it.blockLightLevel(world, pos, u, v) }

      Data(nextBlock!!.type.color, skyLightLevels, blockLightLevels, true, com.fracturedskies.game.render.components.world.Occlusion.of(world, pos, u, v))
    }
    sliceMesh && isOpaque(currentBlock) && isOpaque(nextBlock) && d == Vector3i.AXIS_Y -> {
      val skyLightLevels = com.fracturedskies.game.render.components.world.VertexCorner.values().associate { it to 0f }
      val blockLightLevels = com.fracturedskies.game.render.components.world.VertexCorner.values().associate { it to 0f }

      Data(Color4.DARK_BROWN, skyLightLevels, blockLightLevels, false, com.fracturedskies.game.render.components.world.Occlusion.all)
    }
    else -> null
  }
}
