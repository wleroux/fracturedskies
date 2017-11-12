package com.fracturedskies.render.components.world

import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.math.within
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
          val location = Vector3i(ix, iy, iz)
          val block = world[ix, iy, iz]
          val type = block.type
          if (type == BlockType.AIR) {
            continue
          }

          val xOffset = ix.toFloat()
          val yOffset = iy.toFloat()
          val zOffset = iz.toFloat()

          // north
          val north = location + Vector3i.AXIS_NEG_Z
          if (isEmpty(world, north)) {
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
          val up = location + Vector3i.AXIS_Y
          if (isEmpty(world, up)) {
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
          val west = location + Vector3i.AXIS_NEG_X
          if (isEmpty(world, west)) {
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
          val east = location + Vector3i.AXIS_X
          if (isEmpty(world, east)) {
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
          val down = location + Vector3i.AXIS_NEG_Y
          if (isEmpty(world, down)) {
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
          val south = location + Vector3i.AXIS_Z
          if (isEmpty(world, south)) {
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

  private fun isEmpty(world: World, location: Vector3i): Boolean {
    return if (location within world) {
      val block = world[location.x, location.y, location.z]
      block.type == BlockType.AIR
    } else {
      true
    }
  }
}

