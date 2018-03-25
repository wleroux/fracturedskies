package com.fracturedskies.render.components.world

import com.fracturedskies.api.*
import com.fracturedskies.engine.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.shaders.Mesh
import com.fracturedskies.render.shaders.color.ColorShaderProgram
import org.lwjgl.opengl.GL11.glViewport
import kotlin.math.*

class WorldRenderer(attributes: MultiTypeMap) : Component<Unit>(attributes, Unit) {
  companion object {
    fun Node.Builder<*>.worldRenderer(additionalContext: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::WorldRenderer, additionalContext))
    }

    // Attributes
    val WORLD = TypedKey<ChunkSpace<Block>>("world")
    val TIME_OF_DAY = TypedKey<Float>("timeOfDay")
    val WORKERS = TypedKey<Map<Id, Worker>>("workers")
    val SLICE_HEIGHT = TypedKey<Int>("sliceHeight")
    val VIEW = TypedKey<Vector3>("view")
    val ROTATION = TypedKey<Quaternion4>("rotation")
  }

  private val timeOfDay get() = requireNotNull(props[TIME_OF_DAY])
  private val workers get() = requireNotNull(props[WORKERS])

  private val skyLightDirection = Vector3(0f, -1f, -0.5f).normalize()
  private lateinit var program: ColorShaderProgram
  private lateinit var skyLightLevels: LightLevels
  private lateinit var blockLightLevels: LightLevels
  override fun willMount() {
    super.willMount()
    program = ColorShaderProgram()
    skyLightLevels = LightLevels.load(loadByteBuffer("SkyLightLevels.png", WorldRenderer::class.java), 240)
    blockLightLevels = LightLevels.load(loadByteBuffer("BlockLightLevels.png", WorldRenderer::class.java), 16)

    view = Matrix4(requireNotNull(props[VIEW]), requireNotNull(props[ROTATION])).invert()
    lightDirection = skyLightDirection * Quaternion4(Vector3.AXIS_Z, requireNotNull(props[TIME_OF_DAY]) * 2f * PI.toFloat())
    val sliceHeight = requireNotNull(props[SLICE_HEIGHT])
    val world = requireNotNull(props[WORLD])

    blockMesh = Array(world.chunks.dimension.size, {chunkIndex ->
      generateChunkBlockMesh(world, sliceHeight, world.chunks.dimension.toVector3i(chunkIndex))
    })
    blockSliceMesh = Array(world.chunks.dimension.size, { chunkIndex ->
      generateChunkSliceBlockMesh(world, sliceHeight, world.chunks.dimension.toVector3i(chunkIndex))
    })

    waterMesh = Array(world.chunks.dimension.size, {chunkIndex ->
      generateChunkWaterMesh(world, sliceHeight, world.chunks.dimension.toVector3i(chunkIndex))
    })
    waterSliceMesh = Array(world.chunks.dimension.size, {chunkIndex ->
      generateChunkSliceWaterMesh(world, sliceHeight, world.chunks.dimension.toVector3i(chunkIndex))
    })
  }

  override fun willUnmount() {
    blockMesh.forEach(Mesh::close)
    waterMesh.forEach(Mesh::close)
    blockSliceMesh.forEach(Mesh::close)
    waterSliceMesh.forEach(Mesh::close)
  }

  private val model: Matrix4 = Matrix4(Vector3(0f, 0f, 0f))
  private var projection: Matrix4 = Matrix4.IDENTITY
  private lateinit var view: Matrix4
  private lateinit var lightDirection: Vector3
  private lateinit var blockMesh: Array<Mesh>
  private lateinit var waterMesh: Array<Mesh>
  private lateinit var blockSliceMesh: Array<Mesh>
  private lateinit var waterSliceMesh: Array<Mesh>

  override fun willReceiveProps(nextProps: MultiTypeMap) {
    super.willReceiveProps(nextProps)

    if (nextProps[VIEW] !== props[VIEW] || nextProps[ROTATION] !== props[ROTATION])
      view = Matrix4(requireNotNull(nextProps[VIEW]), requireNotNull(nextProps[ROTATION])).invert()

    if (nextProps[TIME_OF_DAY] !== props[TIME_OF_DAY])
      lightDirection = skyLightDirection * Quaternion4(Vector3.AXIS_Z, requireNotNull(nextProps[TIME_OF_DAY]) * 2f * PI.toFloat())

    val prevSliceHeight = requireNotNull(props[SLICE_HEIGHT])
    val nextSliceHeight = requireNotNull(nextProps[SLICE_HEIGHT])
    val prevWorld = requireNotNull(props[WORLD])
    val nextWorld = requireNotNull(nextProps[WORLD])
    if (nextWorld !== prevWorld || nextSliceHeight != prevSliceHeight) {
      nextWorld.chunks.dimension.forEach { chunkIndex ->
        val chunkPos = nextWorld.chunks.dimension.toVector3i(chunkIndex)
        val sliceHeightChanged = if (prevSliceHeight != nextSliceHeight) {
          val chunkYRange = (chunkPos.y * CHUNK_Y_SIZE until (chunkPos.y + 1) * CHUNK_Y_SIZE)
          (prevSliceHeight) in chunkYRange || (nextSliceHeight) in chunkYRange
        } else {
          false
        }
        val neighborChanged = Vector3i.NEIGHBOURS
            .map { chunkPos + it }
            .filter { nextWorld.chunks.dimension.has(it) }
            .any { nextWorld.chunks[it] !== prevWorld.chunks[it] }
        if (prevWorld.chunks[chunkIndex] !== nextWorld.chunks[chunkIndex] || neighborChanged || sliceHeightChanged) {
          blockMesh[chunkIndex].close()
          blockMesh[chunkIndex] = generateChunkBlockMesh(nextWorld, nextSliceHeight, chunkPos)
          blockSliceMesh[chunkIndex].close()
          blockSliceMesh[chunkIndex] = generateChunkSliceBlockMesh(nextWorld, nextSliceHeight, chunkPos)

          waterMesh[chunkIndex].close()
          waterMesh[chunkIndex] = generateChunkWaterMesh(nextWorld, nextSliceHeight, chunkPos)
          waterSliceMesh[chunkIndex].close()
          waterSliceMesh[chunkIndex] = generateChunkSliceWaterMesh(nextWorld, nextSliceHeight, chunkPos)
        }
      }
    }
  }

  override fun render(bounds: Bounds) {
    if (this.bounds != bounds)
      projection = Matrix4.perspective(Math.PI.toFloat() / 4, bounds.width, bounds.height, 0.03f, 1000f)
    super.render(bounds)

    // Specify Viewport
    glViewport(bounds.x, bounds.y, bounds.width, bounds.height)

    // Draw World
    program.bind {
      model(model)
      view(view)
      projection(projection)
      lightDirection(lightDirection)
      skyColors(skyLightLevels, timeOfDay)
      blockColors(blockLightLevels)

      blockMesh.forEach(::draw)
      blockSliceMesh.forEach(::draw)
      val sliceHeight = requireNotNull(props[SLICE_HEIGHT])
      workers.forEach { _, worker -> if (worker.pos.y < sliceHeight) {
        val workerModel = Matrix4(position = worker.pos.toVector3())
        model(workerModel)
        val block = requireNotNull(props[WORLD])[worker.pos]
        val workerMesh = generateWorkerMesh(block.skyLight.toFloat(), block.blockLight.toFloat()).invoke()
        draw(workerMesh)
        workerMesh.close()
      }}
      model(requireNotNull(model))
      waterMesh.forEach(::draw)
      waterSliceMesh.forEach(::draw)
    }
  }

  private fun generateChunkBlockMesh(space: ChunkSpace<Block>, sliceHeight: Int, chunkPos: Vector3i) = generateWorldMesh(space, false,
      chunkPos.x * CHUNK_X_SIZE until (chunkPos.x + 1) * CHUNK_X_SIZE,
      chunkPos.y * CHUNK_Y_SIZE until min((chunkPos.y + 1) * CHUNK_Y_SIZE, sliceHeight),
      chunkPos.z * CHUNK_Z_SIZE until (chunkPos.z + 1) * CHUNK_Z_SIZE
  ).invoke()
  private fun generateChunkSliceBlockMesh(space: ChunkSpace<Block>, sliceHeight: Int, chunkPos: Vector3i): Mesh {
    return if (sliceHeight in (chunkPos.y * CHUNK_Y_SIZE until (chunkPos.y + 1) * CHUNK_Y_SIZE)) {
      generateWorldMesh(space, true,
          chunkPos.x * CHUNK_X_SIZE until (chunkPos.x + 1) * CHUNK_X_SIZE,
          sliceHeight - 1 until sliceHeight,
          chunkPos.z * CHUNK_Z_SIZE until (chunkPos.z + 1) * CHUNK_Z_SIZE
      ).invoke()
    } else {
      Mesh(FloatArray(0), IntArray(0), emptyList())
    }
  }
  private fun generateChunkWaterMesh(space: ChunkSpace<Block>, sliceHeight: Int, chunkPos: Vector3i) = generateWaterMesh(space, false,
      chunkPos.x * CHUNK_X_SIZE until (chunkPos.x + 1) * CHUNK_X_SIZE,
      chunkPos.y * CHUNK_Y_SIZE until Math.min((chunkPos.y + 1) * CHUNK_Y_SIZE, sliceHeight),
      chunkPos.z * CHUNK_Z_SIZE until (chunkPos.z + 1) * CHUNK_Z_SIZE
  ).invoke()
  private fun generateChunkSliceWaterMesh(space: ChunkSpace<Block>, sliceHeight: Int, chunkPos: Vector3i): Mesh {
    return if (sliceHeight in (chunkPos.y * CHUNK_Y_SIZE until (chunkPos.y + 1) * CHUNK_Y_SIZE)) {
      generateWaterMesh(space, true,
          chunkPos.x * CHUNK_X_SIZE until (chunkPos.x + 1) * CHUNK_X_SIZE,
          sliceHeight - 1 until sliceHeight,
          chunkPos.z * CHUNK_Z_SIZE until (chunkPos.z + 1) * CHUNK_Z_SIZE
      ).invoke()
    } else {
      Mesh(FloatArray(0), IntArray(0), emptyList())
    }
  }
}