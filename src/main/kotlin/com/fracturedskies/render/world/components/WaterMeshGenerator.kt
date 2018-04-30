package com.fracturedskies.render.world.components

import com.fracturedskies.api.MAX_WATER_LEVEL
import com.fracturedskies.api.block.*
import com.fracturedskies.api.block.data.WaterLevel
import com.fracturedskies.engine.collections.Space
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.world.*
import com.fracturedskies.render.world.VertexCorner.*
import org.lwjgl.BufferUtils.*


fun generateWaterMesh(
    world: Space<Block>,
    sliceMesh: Boolean,
    xRange: IntRange, yRange: IntRange, zRange: IntRange
): () -> Mesh {
  val quads = mutableListOf<Quad>()
  for (y in yRange) {
    for (x in xRange) {
      for (z in zRange) {
        val pos = Vector3i(x, y, z)
        val block = world[pos]
        if (block.type !== BlockAir) {
          continue
        }

        val curWaterLevel = waterLevel(block)
        if (curWaterLevel == 0.toByte()) {
          continue
        }

        val xOffset = x.toFloat()
        val yOffset = y.toFloat()
        val zOffset = z.toFloat()
        val curWaterHeight = waterHeight(curWaterLevel)

        // north
        val northPos =  pos + Vector3i.AXIS_NEG_Z
        if (!sliceMesh && isEmpty(world, northPos)) {
          val adjWaterLevel = if (!world.has(northPos)) 0 else waterLevel(world[northPos])
          val adjWaterHeight = waterHeight(adjWaterLevel)

          if (adjWaterHeight < curWaterHeight) {
            val skyLightLevels = VertexCorner.values().associate { it to it.skyLightLevel(world, pos, Vector3i.AXIS_X, Vector3i.AXIS_NEG_Y) }
            val blockLightLevels = VertexCorner.values().associate { it to it.blockLightLevel(world, pos, Vector3i.AXIS_X, Vector3i.AXIS_NEG_Y) }

            quads.add(Quad(
                xOffset, 1f, 0f,
                yOffset + curWaterHeight, 0f, adjWaterHeight - curWaterHeight,
                zOffset, 0f, 0f,
                0f, 0f, -1f,
                skyLightLevels[TOP_LEFT]
                    ?: 0f, skyLightLevels[TOP_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_LEFT] ?: 0f,
                blockLightLevels[TOP_LEFT]
                    ?: 0f, blockLightLevels[TOP_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_LEFT] ?: 0f,
                Color4.WATER,
                0f, 0f, 0f, 0f
            ))
          }
        }


        // up
        val upPos = pos + Vector3i.AXIS_Y
        if (sliceMesh || isEmpty(world, upPos) || curWaterLevel != MAX_WATER_LEVEL) {
          val adjWaterLevel = if (world.has(upPos)) waterLevel(world[upPos]) else 0
          val drawMesh = when (sliceMesh) {
            true -> curWaterLevel == MAX_WATER_LEVEL
            false -> curWaterLevel != MAX_WATER_LEVEL || adjWaterLevel == 0.toByte()
          }
          if (drawMesh) {
            val skyLightLevels = VertexCorner.values().associate { it to it.skyLightLevel(world, pos, Vector3i.AXIS_X, Vector3i.AXIS_NEG_Z) }
            val blockLightLevels = VertexCorner.values().associate { it to it.blockLightLevel(world, pos, Vector3i.AXIS_X, Vector3i.AXIS_NEG_Z) }
            quads.add(Quad(
                xOffset, 1f, 0f,
                yOffset + curWaterHeight, 0f, 0f,
                zOffset + 1f, 0f, -1f,
                0f, 1f, 0f,
                skyLightLevels[TOP_LEFT]
                    ?: 0f, skyLightLevels[TOP_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_LEFT] ?: 0f,
                blockLightLevels[TOP_LEFT]
                    ?: 0f, blockLightLevels[TOP_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_LEFT] ?: 0f,
                Color4.WATER,
                0f, 0f, 0f, 0f
            ))
          }
        }

        // west
        val westPos = pos + Vector3i.AXIS_NEG_X
        if (!sliceMesh && isEmpty(world, westPos)) {
          val adjWaterLevel = if (world.has(westPos)) waterLevel(world[westPos]) else 0
          val adjWaterHeight = waterHeight(adjWaterLevel)

          if (adjWaterHeight < curWaterHeight) {
            val skyLightLevels = VertexCorner.values().associate { it to it.skyLightLevel(world, pos, Vector3i.AXIS_NEG_Z, Vector3i.AXIS_NEG_Y) }
            val blockLightLevels = VertexCorner.values().associate { it to it.blockLightLevel(world, pos, Vector3i.AXIS_NEG_Z, Vector3i.AXIS_NEG_Y) }
            quads.add(Quad(
                xOffset, 0f, 0f,
                yOffset + curWaterHeight, 0f, adjWaterHeight - curWaterHeight,
                zOffset + 1f, -1f, 0f,
                -1f, 0f, 0f,
                skyLightLevels[TOP_LEFT]
                    ?: 0f, skyLightLevels[TOP_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_LEFT] ?: 0f,
                blockLightLevels[TOP_LEFT]
                    ?: 0f, blockLightLevels[TOP_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_LEFT] ?: 0f,
                Color4.WATER,
                0f, 0f, 0f, 0f
            ))
          }
        }

        // east
        val eastPos = pos + Vector3i.AXIS_X
        if (!sliceMesh && isEmpty(world, eastPos)) {
          val adjWaterLevel = if (world.has(eastPos)) waterLevel(world[eastPos]) else 0
          val adjWaterHeight = waterHeight(adjWaterLevel)

          if (adjWaterHeight < curWaterHeight) {
            val skyLightLevels = VertexCorner.values().associate { it to it.skyLightLevel(world, pos, Vector3i.AXIS_Z, Vector3i.AXIS_NEG_Y) }
            val blockLightLevels = VertexCorner.values().associate { it to it.blockLightLevel(world, pos, Vector3i.AXIS_Z, Vector3i.AXIS_NEG_Y) }
            quads.add(Quad(
                xOffset + 1f, 0f, 0f,
                yOffset + curWaterHeight, 0f, adjWaterHeight - curWaterHeight,
                zOffset, 1f, 0f,
                1f, 0f, 0f,
                skyLightLevels[TOP_LEFT]
                    ?: 0f, skyLightLevels[TOP_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_LEFT] ?: 0f,
                blockLightLevels[TOP_LEFT]
                    ?: 0f, blockLightLevels[TOP_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_LEFT] ?: 0f,
                Color4.WATER,
                0f, 0f, 0f, 0f
            ))
          }
        }

        // down
        val downPos = pos + Vector3i.AXIS_NEG_Y
        if (!sliceMesh && isEmpty(world, downPos)) {
          val adjWaterLevel = if (world.has(downPos)) waterLevel(world[downPos]) else 0
          if (adjWaterLevel != MAX_WATER_LEVEL) {
            val skyLightLevels = VertexCorner.values().associate { it to it.skyLightLevel(world, pos, Vector3i.AXIS_X, Vector3i.AXIS_Z) }
            val blockLightLevels = VertexCorner.values().associate { it to it.blockLightLevel(world, pos, Vector3i.AXIS_X, Vector3i.AXIS_Z) }

            quads.add(Quad(
                xOffset, 1f, 0f,
                yOffset, 0f, 0f,
                zOffset, 0f, 1f,
                0f, -1f, 0f,
                skyLightLevels[TOP_LEFT]
                    ?: 0f, skyLightLevels[TOP_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_LEFT] ?: 0f,
                blockLightLevels[TOP_LEFT]
                    ?: 0f, blockLightLevels[TOP_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_LEFT] ?: 0f,
                Color4.WATER,
                0f, 0f, 0f, 0f
            ))
          }
        }

        // south
        val southPos = pos + Vector3i.AXIS_Z
        if (!sliceMesh && isEmpty(world, southPos)) {
          val adjWaterLevel = if (world.has(southPos)) waterLevel(world[southPos]) else 0
          val adjWaterHeight = waterHeight(adjWaterLevel)

          if (adjWaterHeight < curWaterHeight) {
            val skyLightLevels = VertexCorner.values().associate { it to it.skyLightLevel(world, pos, Vector3i.AXIS_NEG_X, Vector3i.AXIS_NEG_Y) }
            val blockLightLevels = VertexCorner.values().associate { it to it.blockLightLevel(world, pos, Vector3i.AXIS_NEG_X, Vector3i.AXIS_NEG_Y) }
            quads.add(Quad(
                xOffset + 1f, -1f, 0f,
                yOffset + curWaterHeight, 0f, adjWaterHeight - curWaterHeight,
                zOffset + 1f, 0f, 0f,
                0f, 0f, 1f,
                skyLightLevels[TOP_LEFT]
                    ?: 0f, skyLightLevels[TOP_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_RIGHT]
                ?: 0f, skyLightLevels[BOTTOM_LEFT] ?: 0f,
                blockLightLevels[TOP_LEFT]
                    ?: 0f, blockLightLevels[TOP_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_RIGHT]
                ?: 0f, blockLightLevels[BOTTOM_LEFT] ?: 0f,
                Color4.WATER,
                0f, 0f, 0f, 0f
            ))
          }
        }
      }
    }
  }

  val attributeSize = Quad.Attributes.fold(0, { acc, attr -> acc + attr.elements * attr.elementSize}) / java.lang.Float.BYTES
  val verticesBuffer = createFloatBuffer(attributeSize * 4 * quads.size)
  val indicesBuffer = createIntBuffer(6 * quads.size)

  var vertexCount = 0
  quads.forEach {quad ->
    verticesBuffer.put(quad.vertices())
    indicesBuffer.put(quad.indices(vertexCount))
    vertexCount += 4
  }

  verticesBuffer.flip()
  val vertices = FloatArray(verticesBuffer.remaining())
  verticesBuffer.get(vertices)

  indicesBuffer.flip()
  val indices = IntArray(indicesBuffer.remaining())
  indicesBuffer.get(indices)

  return { Mesh(vertices, indices, Quad.Attributes) }
}

private fun waterLevel(block: Block): Byte {
  return block[WaterLevel::class]!!.value
}

private fun waterHeight(waterLevel: Byte): Float {
  return map(waterLevel.toFloat(), 0f..MAX_WATER_LEVEL.toFloat(), 0f..1f)
}

private fun isEmpty(world: Space<Block>, pos: Vector3i): Boolean {
  return if (!world.has(pos)) true else world[pos].type === BlockAir
}
