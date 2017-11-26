package com.fracturedskies.render.components.world

import com.fracturedskies.game.BlockType
import com.fracturedskies.game.World
import com.fracturedskies.render.shaders.Mesh
import org.lwjgl.BufferUtils.createFloatBuffer
import org.lwjgl.BufferUtils.createIntBuffer

class WorldMeshGenerator {
  fun generateMesh(world: World, xRange: IntRange, yRange: IntRange, zRange: IntRange): ()->Mesh {
    var verticesBuffer = createFloatBuffer(
            9 * 4 * 6 * xRange.count() * zRange.count()
    )
    var indicesBuffer = createIntBuffer(
            6 * 6 * xRange.count() * zRange.count()
    )

    var vertexCount = 0
    for (iy in yRange) {
      for (ix in xRange) {
        for (iz in zRange) {
          val block = world[ix, iy, iz]
          val type = block.type
          if (type == BlockType.AIR) {
            continue
          }

          val xOffset = ix.toFloat()
          val yOffset = iy.toFloat()
          val zOffset = iz.toFloat()

          // north
          if (isEmpty(world, ix, iy, iz - 1)) {
            val tileIndex = type.front
            // @formatter:off
            verticesBuffer.put(floatArrayOf(
                    xOffset + 0f, yOffset + 1f, zOffset + 0f, 0f, 0f, tileIndex.toFloat(), 0f, -1f, 0f,
                    xOffset + 1f, yOffset + 1f, zOffset + 0f, 0f, 1f, tileIndex.toFloat(), 0f, -1f, 0f,
                    xOffset + 1f, yOffset + 0f, zOffset + 0f, 1f, 1f, tileIndex.toFloat(), 0f, -1f, 0f,
                    xOffset + 0f, yOffset + 0f, zOffset + 0f, 1f, 0f, tileIndex.toFloat(), 0f, -1f, 0f
            ))
            indicesBuffer.put(intArrayOf(
                    vertexCount + 0, vertexCount + 1, vertexCount + 2,
                    vertexCount + 2, vertexCount + 3, vertexCount + 0
            ))
            // @formatter:on
            vertexCount += 4
          }


          // up
          if (isEmpty(world, ix, iy + 1, iz)) {
            val tileIndex = type.top
            // @formatter:off
            verticesBuffer.put(floatArrayOf(
                    xOffset + 0f, yOffset + 1f, zOffset + 1f, 0f, 0f, tileIndex.toFloat(), 0f, 1f, 0f,
                    xOffset + 1f, yOffset + 1f, zOffset + 1f, 0f, 1f, tileIndex.toFloat(), 0f, 1f, 0f,
                    xOffset + 1f, yOffset + 1f, zOffset + 0f, 1f, 1f, tileIndex.toFloat(), 0f, 1f, 0f,
                    xOffset + 0f, yOffset + 1f, zOffset + 0f, 1f, 0f, tileIndex.toFloat(), 0f, 1f, 0f
            ))
            indicesBuffer.put(intArrayOf(
                    vertexCount + 0, vertexCount + 1, vertexCount + 2,
                    vertexCount + 2, vertexCount + 3, vertexCount + 0
            ))
            // @formatter:on
            vertexCount += 4
          }

          // west
          if (isEmpty(world, ix - 1, iy, iz)) {
            val tileIndex = type.left
            // @formatter:off
            verticesBuffer.put(floatArrayOf(
                    xOffset + 0f, yOffset + 1f, zOffset + 1f, 0f, 0f, tileIndex.toFloat(), -1f, 0f, 0f,
                    xOffset + 0f, yOffset + 1f, zOffset + 0f, 0f, 1f, tileIndex.toFloat(), -1f, 0f, 0f,
                    xOffset + 0f, yOffset + 0f, zOffset + 0f, 1f, 1f, tileIndex.toFloat(), -1f, 0f, 0f,
                    xOffset + 0f, yOffset + 0f, zOffset + 1f, 1f, 0f, tileIndex.toFloat(), -1f, 0f, 0f
            ))
            indicesBuffer.put(intArrayOf(
                    vertexCount + 0, vertexCount + 1, vertexCount + 2,
                    vertexCount + 2, vertexCount + 3, vertexCount + 0
            ))
            // @formatter:on
            vertexCount += 4
          }

          // east
          if (isEmpty(world, ix + 1, iy, iz)) {
            val tileIndex = type.right
            // @formatter:off
            verticesBuffer.put(floatArrayOf(
                    xOffset + 1f, yOffset + 1f, zOffset + 0f, 0f, 0f, tileIndex.toFloat(), 1f, 0f, 0f,
                    xOffset + 1f, yOffset + 1f, zOffset + 1f, 0f, 1f, tileIndex.toFloat(), 1f, 0f, 0f,
                    xOffset + 1f, yOffset + 0f, zOffset + 1f, 1f, 1f, tileIndex.toFloat(), 1f, 0f, 0f,
                    xOffset + 1f, yOffset + 0f, zOffset + 0f, 1f, 0f, tileIndex.toFloat(), 1f, 0f, 0f
            ))
            indicesBuffer.put(intArrayOf(
                    vertexCount + 0, vertexCount + 1, vertexCount + 2,
                    vertexCount + 2, vertexCount + 3, vertexCount + 0
            ))
            // @formatter:on
            vertexCount += 4
          }

          // down
          if (isEmpty(world, ix, iy - 1, iz)) {
            val tileIndex = type.bottom
            // @formatter:off
            verticesBuffer.put(floatArrayOf(
                    xOffset + 0f, yOffset + 0f, zOffset + 0f, 0f, 0f, tileIndex.toFloat(), 0f, -1f, 0f,
                    xOffset + 1f, yOffset + 0f, zOffset + 0f, 0f, 1f, tileIndex.toFloat(), 0f, -1f, 0f,
                    xOffset + 1f, yOffset + 0f, zOffset + 1f, 1f, 1f, tileIndex.toFloat(), 0f, -1f, 0f,
                    xOffset + 0f, yOffset + 0f, zOffset + 1f, 1f, 0f, tileIndex.toFloat(), 0f, -1f, 0f
            ))
            indicesBuffer.put(intArrayOf(
                    vertexCount + 0, vertexCount + 1, vertexCount + 2,
                    vertexCount + 2, vertexCount + 3, vertexCount + 0
            ))
            // @formatter:on
            vertexCount += 4
          }

          // south
          if (isEmpty(world, ix, iy, iz + 1)) {
            val tileIndex = type.back
            // @formatter:off
            verticesBuffer.put(floatArrayOf(
                    xOffset + 1f, yOffset + 1f, zOffset + 1f, 0f, 0f, tileIndex.toFloat(), 0f, 0f, 1f,
                    xOffset + 0f, yOffset + 1f, zOffset + 1f, 0f, 1f, tileIndex.toFloat(), 0f, 0f, 1f,
                    xOffset + 0f, yOffset + 0f, zOffset + 1f, 1f, 1f, tileIndex.toFloat(), 0f, 0f, 1f,
                    xOffset + 1f, yOffset + 0f, zOffset + 1f, 1f, 0f, tileIndex.toFloat(), 0f, 0f, 1f
            ))

            indicesBuffer.put(intArrayOf(
                    vertexCount + 0, vertexCount + 1, vertexCount + 2,
                    vertexCount + 2, vertexCount + 3, vertexCount + 0
            ))
            // @formatter:on
            vertexCount += 4
          }
        }
      }

      if (verticesBuffer.remaining() < 9 * 4 * 6 * xRange.count() * zRange.count()) {
        val newVerticesBuffer = createFloatBuffer(verticesBuffer.capacity() + 9 * 4 * 6 * xRange.count() * zRange.count())
        verticesBuffer.flip()
        newVerticesBuffer.put(verticesBuffer)
        verticesBuffer = newVerticesBuffer

        val newIndiciesBuffer = createIntBuffer(indicesBuffer.capacity() + 6 * 6 * xRange.count() * zRange.count())
        indicesBuffer.flip()
        newIndiciesBuffer.put(indicesBuffer)
        indicesBuffer = newIndiciesBuffer
      }
    }

    verticesBuffer.flip()
    val vertices = FloatArray(verticesBuffer.remaining())
    verticesBuffer.get(vertices)

    indicesBuffer.flip()
    val indices = IntArray(indicesBuffer.remaining())
    indicesBuffer.get(indices)

    return {Mesh(vertices, indices, listOf(
            Mesh.Attribute.POSITION,
            Mesh.Attribute.TEXCOORD,
            Mesh.Attribute.NORMAL
    ))}
  }

  private fun isEmpty(world: World, x: Int, y: Int, z: Int): Boolean {
    return if (world.has(x, y, z)) {
      val block = world[x, y, z]
      block.type == BlockType.AIR
    } else {
      true
    }
  }
}

