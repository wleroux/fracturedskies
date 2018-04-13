package com.fracturedskies.render.world.components

import com.fracturedskies.api.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.math.Vector3i.Companion.NEIGHBOURS
import com.fracturedskies.render.common.components.gl.GLMeshRenderer.Companion.meshRenderer
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.world.Block
import kotlin.math.min


class GLBlockRenderer(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.blockRenderer(blocks: ChunkSpace<Block>, chunkPos: Vector3i, sliceHeight: Int, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::GLBlockRenderer, MultiTypeMap(
          BLOCKS to blocks,
          CHUNK_POS to chunkPos,
          SLICE_HEIGHT to sliceHeight
      ).with(additionalProps)))
    }

    val CHUNK_POS = TypedKey<Vector3i>("chunkPos")
    val BLOCKS = TypedKey<ChunkSpace<Block>>("blocks")
    val SLICE_HEIGHT = TypedKey<Int>("sliceHeight")
  }

  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean {
    if (props[CHUNK_POS] != nextProps[CHUNK_POS])
      return true
    val nextChunkPos = nextProps[CHUNK_POS]
    val chunkTop = (nextChunkPos.y + 1) * CHUNK_Y_SIZE
    if (props[SLICE_HEIGHT] > chunkTop && nextProps[SLICE_HEIGHT] <= chunkTop)
      return true
    val chunkBottom = (nextChunkPos.y + 0) * CHUNK_Y_SIZE
    if (props[SLICE_HEIGHT] <= chunkBottom && nextProps[SLICE_HEIGHT] > chunkBottom)
      return true

    val blocks = props[BLOCKS]
    val nextBlocks = nextProps[BLOCKS]
    if (blocks.chunks[nextChunkPos] !== nextBlocks.chunks[nextChunkPos])
      return true
    if (NEIGHBOURS.map { it + nextChunkPos }.filter { blocks.chunks.has(it) }.any { blocks.chunks[it] != nextBlocks.chunks[it] })
      return true

    return false
  }

  var sliceMesh: Mesh? = null
  var mesh: Mesh? = null
  override fun componentWillUpdate(nextProps: MultiTypeMap, nextState: Unit) {
    super.componentWillUpdate(nextProps, nextState)
    val space = nextProps[BLOCKS]
    val chunkPos = nextProps[CHUNK_POS]
    val sliceHeight = nextProps[SLICE_HEIGHT]
    mesh?.close()
    mesh = generateWorldMesh(space, false,
        chunkPos.x * CHUNK_X_SIZE until (chunkPos.x + 1) * CHUNK_X_SIZE,
        chunkPos.y * CHUNK_Y_SIZE until min((chunkPos.y + 1) * CHUNK_Y_SIZE, sliceHeight),
        chunkPos.z * CHUNK_Z_SIZE until (chunkPos.z + 1) * CHUNK_Z_SIZE
    ).invoke()

    sliceMesh?.close()
    sliceMesh = if (sliceHeight in (chunkPos.y * CHUNK_Y_SIZE until (chunkPos.y + 1) * CHUNK_Y_SIZE)) {
      generateWorldMesh(space, true,
          chunkPos.x * CHUNK_X_SIZE until (chunkPos.x + 1) * CHUNK_X_SIZE,
          sliceHeight - 1 until sliceHeight,
          chunkPos.z * CHUNK_Z_SIZE until (chunkPos.z + 1) * CHUNK_Z_SIZE
      ).invoke()
    } else {
      Mesh(FloatArray(0), IntArray(0), emptyList())
    }
  }

  override fun render() = nodes {
    meshRenderer(this@GLBlockRenderer.mesh!!)
    meshRenderer(this@GLBlockRenderer.sliceMesh!!)
  }
}