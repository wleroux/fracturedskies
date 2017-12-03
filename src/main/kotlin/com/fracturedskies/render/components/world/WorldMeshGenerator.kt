package com.fracturedskies.render.components.world

import com.fracturedskies.engine.math.Color4
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.game.BlockType
import com.fracturedskies.game.World
import com.fracturedskies.render.shaders.Mesh
import org.lwjgl.BufferUtils
import java.util.*

/**
 * A greedy mesh implementation
 */
fun generateWorldMesh(
        world: World,
        xRange: IntRange, yRange: IntRange, zRange: IntRange
): () -> Mesh {
  val ranges = arrayOf(xRange, yRange, zRange)
  val dimensions = arrayOf(xRange.count(), yRange.count(), zRange.count())
  val vectors = arrayOf(Vector3i.AXIS_X, Vector3i.AXIS_Y, Vector3i.AXIS_Z)

  //Sweep over 3-axes
  val quads = mutableListOf<Quad>()
  for (d in 0..2) {
    val u = (d + 1) % 3
    val v = (d + 2) % 3
    val mask = arrayOfNulls<Data>(dimensions[u] * dimensions[v])

    val pos = intArrayOf(0, 0, 0)
    pos[d] = ranges[d].start - 1
    while (pos[d] <= ranges[d].endInclusive) {
      //Compute mask
      var n = 0
      ranges[v].forEach { xv ->
        pos[v] = xv
        ranges[u].forEach { xu ->
          pos[u] = xu
          mask[n] = getData(world, Vector3i(pos[0], pos[1], pos[2]), vectors[d], vectors[u], vectors[v])
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
            var quad = Quad(pos, du, dv, vectors[d], data.color, data.occlusion)
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
  val verticesBuffer = BufferUtils.createFloatBuffer(8 * 4 * quads.size)
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

  return {Mesh(vertices, indices, listOf(
          Mesh.Attribute.POSITION,
          Mesh.Attribute.COLOR,
          Mesh.Attribute.OCCLUSION,
          Mesh.Attribute.NORMAL
  ))}
}


private fun getType(world: World, pos: Vector3i): BlockType {
  return if (world.has(pos.x, pos.y, pos.z)) {
    world[pos.x, pos.y, pos.z].type
  } else {
    BlockType.AIR
  }
}

private data class Data(val color: Color4, val reversed: Boolean, val occlusion: EnumSet<Occlusion> )
private fun getData(world: World, pos: Vector3i, d: Vector3i, u: Vector3i, v: Vector3i): Data? {
  val currentBlock = getType(world, pos)
  val nextBlock = getType(world, pos + d)
  return when {
    currentBlock.opaque && !nextBlock.opaque -> Data(currentBlock.color, false, Occlusion.of(world, pos + d, u, v))
    nextBlock.opaque && !currentBlock.opaque -> Data(nextBlock.color, true, Occlusion.of(world, pos, u, v))
    else -> null
  }
}
