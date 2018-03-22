package com.fracturedskies.render.components.world

import com.fracturedskies.*
import com.fracturedskies.api.*
import com.fracturedskies.engine.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.event.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.engine.messages.MessageBus.unregister
import com.fracturedskies.render.events.*
import com.fracturedskies.render.shaders.Mesh
import com.fracturedskies.render.shaders.color.ColorShaderProgram
import com.fracturedskies.render.shaders.standard.StandardShaderProgram
import com.fracturedskies.water.api.MAX_WATER_LEVEL
import kotlinx.coroutines.experimental.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*
import java.lang.Integer.*
import kotlin.math.PI

class WorldRenderer(attributes: MultiTypeMap) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    const val CHUNK_X_SIZE = 16
    const val CHUNK_Y_SIZE = 1
    const val CHUNK_Z_SIZE = 16
    val CHUNKS = Vector3i(CHUNK_X_SIZE, CHUNK_Y_SIZE, CHUNK_Z_SIZE)
    fun Node.Builder<*>.worldRenderer(additionalContext: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::WorldRenderer, additionalContext))
    }
  }

  private var firstBlock: Vector3i? = null
  private var focused = false
  private val controller = Controller()
  private val sliceHeight: Int
    get() = world.dimension.height - clamp(controller.slice, 0 until world.dimension.height)

  override val handler = EventHandlers(on(Key::class) { key ->
    if (key.action == GLFW_PRESS) {
      controller.press(key.key)
    } else if (key.action == GLFW_RELEASE){
      controller.release(key.key)
    }
  }, on(Focus::class) {
    focused = true
  }, on(Unfocus::class) {
    focused = false
    controller.clear()
  }, on(Scroll::class) { event ->
    controller.scroll(event.xOffset, event.yOffset)
  },on(Click::class) { event->
    if (!focused) {
      return@on
    }
    if (event.action != GLFW_PRESS && event.action != GLFW_RELEASE) {
      return@on
    }
    event.stopPropogation = true

    val mx = event.mousePos.x.toFloat()
    val my = event.mousePos.y.toFloat()
    val sw = this.bounds.width.toFloat()
    val sh = this.bounds.height.toFloat()
    val sx = -((sw - mx) / sw - 0.5f) * 2
    val sy = (my / sh - 0.5f) * 2

    val perspectiveInverse = Matrix4.perspective(Math.PI.toFloat() / 4, this.bounds.width, this.bounds.height, 0.03f, 1000f).invert()
    val viewInverse = Matrix4(controller.view, controller.rotation)

    var rayStart4 = Vector4(sx, sy, -1f, 1f) * perspectiveInverse * viewInverse
    rayStart4 *= (1f / rayStart4.w)
    val rayStart3 = Vector3(rayStart4) - Vector3.ZERO

    var rayEnd4 = Vector4(sx, sy, 1f, 1f) * perspectiveInverse * viewInverse
    rayEnd4 *= (1f / rayEnd4.w)
    val rayEnd3 = Vector3(rayEnd4) - Vector3.ZERO
    val direction = (rayEnd3 - rayStart3).normalize()

    val selectedBlock = raycast(world, rayStart3, direction)
            .filter { it.position.y < sliceHeight }
            .filterNot { it.obj.type == BlockType.AIR }
            .firstOrNull()
    if (selectedBlock == null) {
      firstBlock = null
    } else {
      when (event.action) {
        GLFW_PRESS -> {
          // Add Blocks or Add Water
          if (event.button == GLFW_MOUSE_BUTTON_LEFT || event.button == GLFW_MOUSE_BUTTON_MIDDLE) {
            firstBlock = selectedBlock.position + selectedBlock.faces.first()
          }
          // Add Water or Remove Blocks
          else if (event.button == GLFW_MOUSE_BUTTON_RIGHT) {
            firstBlock = selectedBlock.position
          }
        }
        GLFW_RELEASE -> {
          if (firstBlock != null) {
            // Add Blocks
            var secondBlock: Vector3i? = null
            if (event.button == GLFW_MOUSE_BUTTON_LEFT || event.button == GLFW_MOUSE_BUTTON_MIDDLE) {
              secondBlock = selectedBlock.position + selectedBlock.faces.first()
            }
            // Add Water or Remove Blocks
            else if (event.button == GLFW_MOUSE_BUTTON_RIGHT) {
              secondBlock = selectedBlock.position
            }

            val xRange = min(firstBlock!!.x, secondBlock!!.x)..max(firstBlock!!.x, secondBlock.x)
            val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
            val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)

            // Add Blocks
            if (event.button == GLFW_MOUSE_BUTTON_LEFT) {
              if (!controller.isPressed(GLFW_KEY_LEFT_CONTROL)) {
                val blockType = when {
                  controller.isPressed(GLFW_KEY_LEFT_SHIFT) -> BlockType.LIGHT
                  else -> BlockType.BLOCK
                }

                val updates = xRange.flatMap { x ->
                  zRange.flatMap { z ->
                    yRange.flatMap { y ->
                      if (world.has(x, y, z) && !world[x, y, z].type.opaque)
                        listOf(Vector3i(x, y, z) to blockType)
                      else listOf()
                    }
                  }
                }.toMap()
                send(UpdateBlock(updates, Cause.of(this)))
              } else {
                send(SpawnWorker(Id(), Vector3i(xRange.start, yRange.start, zRange.start), Cause.of(this)))
              }
            }
            // Remove Blocks
            else if (event.button == GLFW_MOUSE_BUTTON_RIGHT) {
              val updates = xRange.flatMap { x ->
                zRange.flatMap { z ->
                  yRange.flatMap { y ->
                    if (world.has(x, y, z) && world[x, y, z].type.opaque)
                      listOf(Vector3i(x, y, z) to BlockType.AIR)
                    else listOf()
                  }
                }
              }.toMap()
              send(UpdateBlock(updates, Cause.of(this)))
            }
            // Add Water
            else if (event.button == GLFW_MOUSE_BUTTON_MIDDLE) {
              val updates = xRange.flatMap { x ->
                zRange.flatMap { z ->
                  yRange.flatMap { y ->
                    if (world.has(x, y, z) && !world[x, y, z].type.opaque) {
                      if (event.button == GLFW_MOUSE_BUTTON_1) {
                        val waterLevel = world[x, y, z].waterLevel
                        if (waterLevel > 0.toByte()) {
                          listOf(Vector3i(x, y, z) to waterLevel.dec())
                        } else listOf()
                      } else {
                        val waterLevel = world[x, y, z].waterLevel
                        if (waterLevel < MAX_WATER_LEVEL) {
                          listOf(Vector3i(x, y, z) to MAX_WATER_LEVEL)
                        } else listOf()
                      }
                    } else listOf()
                  }
                }
              }.toMap()

              send(UpdateBlockWater(updates, Cause.of(this)))
            }
          }
          firstBlock = null
        }
      }
    }
  })

  private var initialized = false
  private lateinit var program: ColorShaderProgram
  private lateinit var world: ObjectMap<Block>
  private var workers = mutableMapOf<Id, Worker>()
  private var timeOfDay = 0f
  private var channel = MessageChannel(context = UI_CONTEXT) { message ->
    when (message) {
      is NewGameRequested -> {
        world = ObjectMap(message.dimension) { Block(BlockType.AIR, 0, 0, 0)}
        initialized = true
      }
      is UpdateBlock -> {
        message.updates.forEach { pos, type ->
          world[pos].type = type
        }
        (Vector3i.NEIGHBOURS + Vector3i.ADDITIVE_UNIT)
            .flatMap { message.updates.map { (pos, _) -> pos + it } }
            .map { it / CHUNKS }
            .filter {
              it.x in (0 until world.dimension.width / CHUNK_X_SIZE) &&
                  it.y in (0 until world.dimension.height / CHUNK_Y_SIZE) &&
                  it.z in (0 until world.dimension.depth / CHUNK_Z_SIZE)
            }
            .distinct()
            .forEach { updateBlockMesh(world, it) }
      }
      is SkyLightUpdated -> {
        message.updates.forEach { pos, skyLightLevel ->
          world[pos].skyLight = skyLightLevel
        }
        message.updates
            .map { (pos, _) -> pos }
            .flatMap { pos -> Vector3i.NEIGHBOURS.map { pos + it } }
            .map { pos -> pos / CHUNKS }
            .filter {
              it.x in (0 until world.dimension.width / CHUNK_X_SIZE) &&
                  it.y in (0 until world.dimension.height / CHUNK_Y_SIZE) &&
                  it.z in (0 until world.dimension.depth / CHUNK_Z_SIZE)
            }
            .distinct()
            .forEach {
              updateBlockMesh(world, it)
              updateWaterMesh(world, it)
            }
      }
      is BlockLightUpdated -> {
        message.updates.forEach { pos, blockLightLevel ->
          world[pos].blockLight = blockLightLevel
        }
        message.updates
            .map { (pos, _) -> pos }
            .flatMap { pos -> Vector3i.NEIGHBOURS.map { pos + it } }
            .map { pos -> pos / CHUNKS }
            .filter {
              it.x in (0 until world.dimension.width / CHUNK_X_SIZE) &&
                  it.y in (0 until world.dimension.height / CHUNK_Y_SIZE) &&
                  it.z in (0 until world.dimension.depth / CHUNK_Z_SIZE)
            }
            .distinct()
            .forEach {
              updateBlockMesh(world, it)
              updateWaterMesh(world, it)
            }
      }
      is UpdateBlockWater -> {
        message.updates.forEach { (pos, waterLevel) ->
          world[pos].waterLevel = waterLevel
        }
        (Vector3i.NEIGHBOURS + Vector3i.ADDITIVE_UNIT)
            .flatMap { message.updates.map { (pos, _) -> pos + it } }
            .map { it / CHUNKS }
            .filter {
              it.x in (0 until world.dimension.width / CHUNK_X_SIZE) &&
                  it.y in (0 until world.dimension.height / CHUNK_Y_SIZE) &&
                  it.z in (0 until world.dimension.depth / CHUNK_Z_SIZE)
            }
            .distinct()
            .forEach { updateWaterMesh(world, it) }
      }
      is SpawnWorker -> {
        workers[message.id] = Worker(message.initialPos)
      }
      is MoveWorkers -> {
        message.movements.forEach { id, nextPos ->
          workers[id]!!.pos = nextPos
        }
      }
      is TimeUpdated -> {
        timeOfDay = message.time
      }
    }
  }

  private var blockMesh = mutableMapOf<Vector3i, Mesh>()
  private var blockSliceMesh = mutableMapOf<Vector3i, Mesh>()
  private var renderBlockMeshJob = mutableMapOf<Vector3i, Job>()
  private fun updateBlockMesh(world: ObjectMap<Block>, chunk: Vector3i) {
    renderBlockMeshJob[chunk]?.cancel()
    renderBlockMeshJob[chunk] = launch {
      val meshGenerator = generateWorldMesh(world, false,
              (chunk.x * CHUNK_X_SIZE) until ((chunk.x + 1) * CHUNK_X_SIZE),
              (chunk.y * CHUNK_Y_SIZE) until ((chunk.y + 1) * CHUNK_Y_SIZE),
              (chunk.z * CHUNK_Z_SIZE) until ((chunk.z + 1) * CHUNK_Z_SIZE))
      val sliceMeshGenerator = generateWorldMesh(world, true,
              (chunk.x * CHUNK_X_SIZE) until ((chunk.x + 1) * CHUNK_X_SIZE),
              (chunk.y * CHUNK_Y_SIZE) until ((chunk.y + 1) * CHUNK_Y_SIZE),
              (chunk.z * CHUNK_Z_SIZE) until ((chunk.z + 1) * CHUNK_Z_SIZE))

      launch(coroutineContext + UI_CONTEXT) {
        if (isActive) {
          blockSliceMesh[chunk]?.close()
          blockSliceMesh[chunk] = sliceMeshGenerator()

          blockMesh[chunk]?.close()
          blockMesh[chunk] = meshGenerator()
        }
      }
    }
  }

  private var waterMesh = mutableMapOf<Vector3i, Mesh>()
  private var waterSliceMesh = mutableMapOf<Vector3i, Mesh>()
  private var renderWaterMeshJob = mutableMapOf<Vector3i, Job>()
  private fun updateWaterMesh(world: ObjectMap<Block>, chunk: Vector3i) {
    renderWaterMeshJob[chunk]?.cancel()
    renderWaterMeshJob[chunk] = launch {
      val waterMeshGenerator = generateWaterMesh(world, false,
              (chunk.x * CHUNK_X_SIZE) until ((chunk.x + 1) * CHUNK_X_SIZE),
              (chunk.y * CHUNK_Y_SIZE) until ((chunk.y + 1) * CHUNK_Y_SIZE),
              (chunk.z * CHUNK_Z_SIZE) until ((chunk.z + 1) * CHUNK_Z_SIZE))
      val waterSliceMeshGenerator = generateWaterMesh(world, true,
              (chunk.x * CHUNK_X_SIZE) until ((chunk.x + 1) * CHUNK_X_SIZE),
              (chunk.y * CHUNK_Y_SIZE) until ((chunk.y + 1) * CHUNK_Y_SIZE),
              (chunk.z * CHUNK_Z_SIZE) until ((chunk.z + 1) * CHUNK_Z_SIZE))

      launch(coroutineContext + UI_CONTEXT) {
        if (isActive) {
          waterSliceMesh[chunk]?.close()
          waterSliceMesh[chunk] = waterSliceMeshGenerator()

          waterMesh[chunk]?.close()
          waterMesh[chunk] = waterMeshGenerator()
        }
      }
    }
  }

  private val skyLightDirection = Vector3(0f, -1f, -0.5f).normalize()
  private lateinit var listener: MessageChannel
  private lateinit var skyLightLevels: LightLevels
  private lateinit var blockLightLevels: LightLevels
  override fun willMount() = runBlocking<Unit> {
    program = ColorShaderProgram()
    skyLightLevels = LightLevels.load(loadByteBuffer("SkyLightLevels.png", WorldRenderer::class.java), 240)
    blockLightLevels = LightLevels.load(loadByteBuffer("BlockLightLevels.png", WorldRenderer::class.java), 16)
    listener = register(channel)
    controller.register()
    send(NewGameRequested(Dimension(4*CHUNK_X_SIZE, 32 * CHUNK_Y_SIZE, 4*CHUNK_Z_SIZE), Cause.of(this@WorldRenderer)))
  }

  override fun willUnmount() {
    unregister(listener)
    controller.unregister()
  }

  override fun render(bounds: Bounds) {
    super.render(bounds)
    if (!initialized)
      return

    controller.viewCenter.y = run {
      val yRange = (0 until sliceHeight)
      val viewHeight = heightAt(world, controller.view.x.toInt(), controller.view.z.toInt(), yRange).toFloat()
      val viewCenterHeight = heightAt(world, controller.viewCenter.x.toInt(), controller.viewCenter.z.toInt(), yRange).toFloat()
      val minimumHeight = viewHeight + 5f
      val desiredHeight = viewCenterHeight + controller.viewOffset.y

      Math.max(minimumHeight, desiredHeight) - controller.viewOffset.y
    }

    val variables = MultiTypeMap(
            StandardShaderProgram.MODEL to Matrix4(Vector3(0f, 0f, 0f)),
            StandardShaderProgram.VIEW to Matrix4(controller.view, controller.rotation).invert(),
            StandardShaderProgram.PROJECTION to Matrix4.perspective(Math.PI.toFloat() / 4, bounds.width, bounds.height, 0.03f, 1000f)
    )

    // Render Texture
    glViewport(0, 0, bounds.width, bounds.height)
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    glEnable(GL_DEPTH_TEST)

    // Draw World
    program.bind {
      model(requireNotNull(variables[StandardShaderProgram.MODEL]))
      view(requireNotNull(variables[StandardShaderProgram.VIEW]))
      projection(requireNotNull(variables[StandardShaderProgram.PROJECTION]))
      lightDirection(skyLightDirection * Quaternion4(Vector3.AXIS_Z, timeOfDay * 2f * PI.toFloat() ))
      skyColors(skyLightLevels, timeOfDay)
      blockColors(blockLightLevels)

      blockMesh.forEach { pos, mesh -> if (pos.y < sliceHeight) draw(mesh) }
      blockSliceMesh.forEach { pos, mesh -> if (pos.y == sliceHeight - 1) draw(mesh) }
      workers.forEach { _, worker -> if (worker.pos.y < sliceHeight) {
        val workerModel = Matrix4(position = worker.pos.toVector3())
        model(workerModel)
        val block = world[worker.pos]
        val workerMesh = generateWorkerMesh(block.skyLight.toFloat(), block.blockLight.toFloat()).invoke()
        draw(workerMesh)
      }}
      model(requireNotNull(variables[StandardShaderProgram.MODEL]))
      waterMesh.forEach { pos, mesh -> if (pos.y < sliceHeight) draw(mesh) }
      waterSliceMesh.forEach { pos, mesh -> if (pos.y == sliceHeight - 1) draw(mesh) }
    }
  }

  private fun heightAt(world: ObjectMap<Block>, x: Int, z: Int, yRange: IntRange): Int {
    val clampedX = clamp(x, 0 until world.dimension.width)
    val clampedZ = clamp(z, 0 until world.dimension.depth)
    return yRange
            .reversed()
            .firstOrNull { world[clampedX, it, clampedZ].type != BlockType.AIR }
            ?: 0
  }
}