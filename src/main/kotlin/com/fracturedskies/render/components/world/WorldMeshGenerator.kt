package com.fracturedskies.render.components.world

import com.fracturedskies.game.BlockType
import com.fracturedskies.game.World
import com.fracturedskies.render.shaders.Mesh
import org.lwjgl.BufferUtils

/**
 * A greedy mesh implementation
 */
fun generateWorldMesh(
        world: World,
        xRange: IntRange, yRange: IntRange, zRange: IntRange
): () -> Mesh {
  val ranges = arrayOf(xRange, yRange, zRange)
  val dimensions = arrayOf(xRange.count(), yRange.count(), zRange.count())

  //Sweep over 3-axes
  val quads = mutableListOf<Quad>()
  for (d in 0..2) {
    val u = (d + 1) % 3
    val v = (d + 2) % 3
    val mask = IntArray(dimensions[u] * dimensions[v])

    val x = intArrayOf(0, 0, 0)
    val q = intArrayOf(0, 0, 0)
    q[d] = 1

    x[d] = ranges[d].start - 1
    while (x[d] <= ranges[d].endInclusive) {
      //Compute mask
      var n = 0
      ranges[v].forEach { xv ->
        x[v] = xv
        ranges[u].forEach { xu ->
          x[u] = xu
          val currentBlock = getBlock(world, x[0], x[1], x[2])
          val nextBlock = getBlock(world, x[0] + q[0], x[1] + q[1], x[2] + q[2])
          mask[n] = if (currentBlock != BlockType.AIR && nextBlock != BlockType.AIR) {
            0x0
          } else if (currentBlock != BlockType.AIR) {
            currentBlock.ordinal
          } else {
            -nextBlock.ordinal
          }
          n ++
        }
      }

      //Increment x[d]
      ++x[d]

      //Generate mesh for mask using lexicographic ordering
      n = 0
      x[v] = ranges[v].start
      while (ranges[v].contains(x[v])) {
        x[u] = ranges[u].start
        while (ranges[u].contains(x[u])) {
          var width = 1
          var height = 1

          val data = mask[n]
          if (data != 0x0) {
            // Get width
            while (ranges[u].contains(x[u] + width) && data == mask[n + width]) {
              width++
            }

            // Get height
            findHeight@ while (ranges[v].contains(x[v] + height)) {
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
            val quad = if (data > 0) {
              Quad(
                      x[0].toFloat(), du[0].toFloat(), dv[0].toFloat(),
                      x[1].toFloat(), du[1].toFloat(), dv[1].toFloat(),
                      x[2].toFloat(), du[2].toFloat(), dv[2].toFloat(),
                      q[0].toFloat(), q[1].toFloat(), q[2].toFloat(),
                      BlockType.values()[data].color
              )
            } else {
              Quad(
                      x[0].toFloat() + du[0].toFloat(), -du[0].toFloat(), dv[0].toFloat(),
                      x[1].toFloat() + du[1].toFloat(), -du[1].toFloat(), dv[1].toFloat(),
                      x[2].toFloat() + du[2].toFloat(), -du[2].toFloat(), dv[2].toFloat(),
                      q[0].toFloat(), q[1].toFloat(), q[2].toFloat(),
                      BlockType.values()[-data].color
              )
            }
            quads.add(quad)

            // Clear quad's mask
            for (i in 0 until height) {
              for (j in 0 until width) {
                mask[n + j + i * dimensions[u]] = 0x0
              }
            }
          }

          //Increment counters and continue
          x[u] += width
          n += width
        }
        x[v]++
      }
    }
  }

  // Generate Mesh
  val verticesBuffer = BufferUtils.createFloatBuffer(7 * 4 * quads.size)
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
          Mesh.Attribute.NORMAL
  ))}
}

fun getBlock(world: World, x: Int, y: Int, z: Int): BlockType {
  return if (world.has(x, y, z)) {
    world[x, y, z].type
  } else {
    BlockType.AIR
  }
}
