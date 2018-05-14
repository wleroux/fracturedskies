package com.fracturedskies.render.world.components

import com.fracturedskies.api.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.DirtyFlags
import com.fracturedskies.render.common.components.gl.glUniform
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import javax.inject.Inject
import kotlin.math.min


class WaterRenderer : Component<Unit>(Unit) {
  companion object {
    fun Node.Builder<*>.water(sliceHeight: Int, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(WaterRenderer::class, MultiTypeMap(
          SLICE_HEIGHT to sliceHeight
      ).with(additionalProps)))
    }

    private val SLICE_HEIGHT = TypedKey<Int>("sliceHeight")
  }

  @Inject
  private lateinit var world: World

  @Inject
  private lateinit var dirtyFlags: DirtyFlags

  override fun componentDidMount() {
    chunks = world.dimension / CHUNK_DIMENSION
    chunksMesh = ObjectMutableSpace(chunks, { null })
    chunksSliceMesh = ObjectMutableSpace(chunks, { null })
  }

  private lateinit var chunks : Dimension
  private lateinit var chunksMesh : ObjectMutableSpace<Mesh?>
  private lateinit var chunksSliceMesh : ObjectMutableSpace<Mesh?>

  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean = false

  private var prevSliceHeight = 0
  override fun glRender(bounds: Bounds) {
    super.glRender(bounds)

    glUniform(ColorShaderProgram.MODEL_LOCATION, Matrix4.IDENTITY)
    val sliceHeight = props[SLICE_HEIGHT]
    val chunks = world.dimension / CHUNK_DIMENSION
    chunks.forEach { chunkIndex ->
      val chunkPos = chunks.vector3i(chunkIndex)
      val shouldChunkUpdate = shouldChunkUpdate(chunkPos)
      val mesh = if (shouldChunkUpdate) {
        val prevMesh = chunksMesh[chunkIndex]
        prevMesh?.close()
        val newMesh = generateWaterMesh(world.blocks, false,
            chunkPos.x * CHUNK_X_SIZE until (chunkPos.x + 1) * CHUNK_X_SIZE,
            chunkPos.y * CHUNK_Y_SIZE until min((chunkPos.y + 1) * CHUNK_Y_SIZE, sliceHeight),
            chunkPos.z * CHUNK_Z_SIZE until (chunkPos.z + 1) * CHUNK_Z_SIZE
        )
        chunksMesh[chunkIndex] = newMesh
        newMesh
      } else {
        chunksMesh[chunkIndex]
      }
      if (mesh != null) {
        glBindVertexArray(mesh.vao)
        glDrawElements(GL11.GL_TRIANGLES, mesh.indexCount, GL_UNSIGNED_INT, 0)
      }

      val sliceMesh = if (shouldChunkUpdate) {
        val prevSliceMesh = chunksSliceMesh[chunkIndex]
        prevSliceMesh?.close()
        val nextSliceMesh = if (sliceHeight in (chunkPos.y * CHUNK_Y_SIZE until (chunkPos.y + 1) * CHUNK_Y_SIZE)) {
          generateWaterMesh(world.blocks, true,
              chunkPos.x * CHUNK_X_SIZE until (chunkPos.x + 1) * CHUNK_X_SIZE,
              sliceHeight - 1 until sliceHeight,
              chunkPos.z * CHUNK_Z_SIZE until (chunkPos.z + 1) * CHUNK_Z_SIZE
          )
        } else {
          null
        }
        chunksSliceMesh[chunkIndex] = nextSliceMesh
        nextSliceMesh
      } else {
        chunksSliceMesh[chunkIndex]
      }
      if (sliceMesh != null) {
        glBindVertexArray(sliceMesh.vao)
        glDrawElements(GL11.GL_TRIANGLES, sliceMesh.indexCount, GL_UNSIGNED_INT, 0)
      }
    }

    prevSliceHeight = sliceHeight
  }

  private fun shouldChunkUpdate(chunkPos: Vector3i): Boolean {
    if (dirtyFlags.blocksDirty[chunkPos]) return true

    val sliceHeight = props[SLICE_HEIGHT]
    if (prevSliceHeight != sliceHeight) {
      // if both slice height is higher than chunk pos, ignore
      val chunkTop = (chunkPos.y + 1) * CHUNK_Y_SIZE
      if (sliceHeight > chunkTop && prevSliceHeight > chunkTop) return false

      // if both slice height is lower than chunk pos, ignore
      val chunkBottom = (chunkPos.y + 0) * CHUNK_Z_SIZE
      if (sliceHeight < chunkBottom && prevSliceHeight < chunkBottom) return false

      return true
    } else {
      return false
    }
  }
}