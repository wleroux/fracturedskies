package com.fracturedskies.render.components.world

import com.fracturedskies.engine.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.shaders.Mesh
import com.fracturedskies.render.shaders.color.ColorShaderProgram
import org.lwjgl.opengl.GL11.glViewport
import kotlin.math.PI

class WorldRenderer(attributes: MultiTypeMap) : Component<Unit>(attributes, Unit) {
  companion object {
    fun Node.Builder<*>.worldRenderer(additionalContext: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::WorldRenderer, additionalContext))
    }

    // Attributes
    val WORLD = TypedKey<ObjectSpace<Block>>("world")
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

    blockMesh = generateWorldMesh(world, false, 0 until world.width, 0 until sliceHeight, 0 until world.depth).invoke()
    blockSliceMesh = generateWorldMesh(world, true, 0 until world.width, sliceHeight - 1 until sliceHeight, 0 until world.depth).invoke()
    waterMesh = generateWaterMesh(world, false, 0 until world.width, 0 until sliceHeight, 0 until world.depth).invoke()
    waterSliceMesh = generateWaterMesh(world, true, 0 until world.width, sliceHeight - 1 until sliceHeight, 0 until world.depth).invoke()
  }

  override fun willUnmount() {
    blockMesh.close()
    waterMesh.close()
    blockSliceMesh.close()
    waterSliceMesh.close()
  }

  private val model: Matrix4 = Matrix4(Vector3(0f, 0f, 0f))
  private var projection: Matrix4 = Matrix4.IDENTITY
  private lateinit var view: Matrix4
  private lateinit var lightDirection: Vector3
  private lateinit var blockMesh: Mesh
  private lateinit var waterMesh: Mesh
  private lateinit var blockSliceMesh: Mesh
  private lateinit var waterSliceMesh: Mesh

  override fun willReceiveProps(nextProps: MultiTypeMap) {
    super.willReceiveProps(nextProps)
    if (nextProps[VIEW] !== props[VIEW] || nextProps[ROTATION] !== props[ROTATION])
      view = Matrix4(requireNotNull(nextProps[VIEW]), requireNotNull(nextProps[ROTATION])).invert()
    if (nextProps[TIME_OF_DAY] !== props[TIME_OF_DAY])
      lightDirection = skyLightDirection * Quaternion4(Vector3.AXIS_Z, requireNotNull(nextProps[TIME_OF_DAY]) * 2f * PI.toFloat())
    if (nextProps[WORLD] !== props[WORLD] || nextProps[SLICE_HEIGHT] !== props[SLICE_HEIGHT]) {
      val sliceHeight = requireNotNull(nextProps[SLICE_HEIGHT])
      val world = requireNotNull(nextProps[WORLD])
      blockMesh.close()
      blockMesh = generateWorldMesh(world, false, 0 until world.width, 0 until sliceHeight, 0 until world.depth).invoke()
      blockSliceMesh.close()
      blockSliceMesh = generateWorldMesh(world, true, 0 until world.width, sliceHeight - 1 until sliceHeight, 0 until world.depth).invoke()

      waterMesh.close()
      waterMesh = generateWaterMesh(world, false, 0 until world.width, 0 until sliceHeight, 0 until world.depth).invoke()
      waterSliceMesh.close()
      waterSliceMesh = generateWaterMesh(world, true, 0 until world.width, sliceHeight - 1 until sliceHeight, 0 until world.depth).invoke()
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

      draw(blockMesh)
      draw(blockSliceMesh)
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
      draw(waterMesh)
      draw(waterSliceMesh)
    }
  }
}