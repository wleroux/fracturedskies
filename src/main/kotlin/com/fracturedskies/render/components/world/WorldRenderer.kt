package com.fracturedskies.render.components.world

import com.fracturedskies.UI_CONTEXT
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.event.*
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.unregister
import com.fracturedskies.game.*
import com.fracturedskies.game.messages.*
import com.fracturedskies.game.water.WaterSystem.Companion.MAX_WATER_LEVEL
import com.fracturedskies.render.events.*
import com.fracturedskies.render.events.Key
import com.fracturedskies.render.shaders.Mesh
import com.fracturedskies.render.shaders.color.ColorShaderProgram
import com.fracturedskies.render.shaders.standard.StandardShaderProgram
import kotlinx.coroutines.experimental.*
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11.*
import java.lang.Integer.*
import kotlin.math.PI

class WorldRenderer(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    val CHUNK_X_SIZE = 128
    val CHUNK_Y_SIZE = 1
    val CHUNK_Z_SIZE = 128
    val CHUNKS = Vector3i(CHUNK_X_SIZE, CHUNK_Y_SIZE, CHUNK_Z_SIZE)
    fun Node.Builder<*>.worldRenderer(additionalContext: Context = Context()) {
      nodes.add(Node(::WorldRenderer, additionalContext))
    }
  }

  private var firstBlock: Vector3i? = null
  private var focused = false
  private val controller = Controller()
  private val sliceHeight: Int
    get() = (world?.dimension?.height ?: 0) - clamp(controller.slice, 0 until (world?.dimension?.height ?: 0))

  override val handler = EventHandlers(on(Key::class) { key ->
    if (key.action == GLFW.GLFW_PRESS) {
      controller.press(key.key)
    } else if (key.action == GLFW.GLFW_RELEASE){
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
    if (event.action != GLFW.GLFW_PRESS && event.action != GLFW.GLFW_RELEASE) {
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

    val selectedBlock = raycast(world!!, rayStart3, direction)
            .filter { it.position.y < sliceHeight }
            .filterNot { it.block.type == BlockType.AIR }
            .firstOrNull()
    if (selectedBlock == null) {
      firstBlock = null
    } else {
      when (event.action) {
        GLFW.GLFW_PRESS -> {
          // Add Blocks or Add Water
          if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT || event.button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            firstBlock = selectedBlock.position + selectedBlock.faces.first()
          }
          // Add Water or Remove Blocks
          else if (event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            firstBlock = selectedBlock.position
          }
        }
        GLFW.GLFW_RELEASE -> {
          if (firstBlock != null) {
            // Add Blocks
            var secondBlock: Vector3i? = null
            if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT || event.button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
              secondBlock = selectedBlock.position + selectedBlock.faces.first()
            }
            // Add Water or Remove Blocks
            else if (event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
              secondBlock = selectedBlock.position
            }

            val xRange = min(firstBlock!!.x, secondBlock!!.x)..max(firstBlock!!.x, secondBlock.x)
            val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
            val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)

            // Add Blocks
            if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
              val updates = xRange.flatMap { x ->
                zRange.flatMap { z ->
                  yRange.flatMap { y ->
                    if (world!!.has(x, y, z) && !world!![x, y, z].type.opaque)
                      listOf(Vector3i(x, y, z) to (if (controller.isPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) BlockType.LIGHT else BlockType.BLOCK))
                    else listOf()
                  }
                }
              }.toMap()
              MessageBus.send(UpdateBlock(updates, Cause.of(this), Context()))
            }
            // Remove Blocks
            else if (event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
              val updates = xRange.flatMap { x ->
                zRange.flatMap { z ->
                  yRange.flatMap { y ->
                    if (world!!.has(x, y, z) && world!![x, y, z].type.opaque)
                      listOf(Vector3i(x, y, z) to BlockType.AIR)
                    else listOf()
                  }
                }
              }.toMap()
              MessageBus.send(UpdateBlock(updates, Cause.of(this), Context()))
            }
            // Add Water
            else if (event.button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
              val updates = xRange.flatMap { x ->
                zRange.flatMap { z ->
                  yRange.flatMap { y ->
                    if (world!!.has(x, y, z) && !world!![x, y, z].type.opaque) {
                      if (event.button == GLFW.GLFW_MOUSE_BUTTON_1) {
                        val waterLevel = world!![x, y, z].waterLevel
                        if (waterLevel > 0.toByte()) {
                          listOf(Vector3i(x, y, z) to waterLevel.dec())
                        } else listOf()
                      } else {
                        val waterLevel = world!![x, y, z].waterLevel
                        if (waterLevel < MAX_WATER_LEVEL) {
                          listOf(Vector3i(x, y, z) to MAX_WATER_LEVEL)
                        } else listOf()
                      }
                    } else listOf()
                  }
                }
              }.toMap()

              MessageBus.send(UpdateBlockWater(updates, Cause.of(this), Context()))
            }
          }
          firstBlock = null
        }
      }
    }
  })

  lateinit private var program: ColorShaderProgram
  var world: ObjectMap<Block>? = null
  var timeOfDay = 0f
  private var channel = MessageChannel(coroutineContext = UI_CONTEXT) { message ->
    when (message) {
      is WorldGenerated -> {
        world = ObjectMap(message.world.dimension) { message.world[it] }
        val world = this.world!!
        val xChunks = world.dimension.width / CHUNK_X_SIZE
        val yChunks = world.dimension.height / CHUNK_Y_SIZE
        val zChunks = world.dimension.depth / CHUNK_Z_SIZE
        (0 until xChunks).forEach { xChunk ->
          (0 until yChunks).forEach { yChunk ->
            (0 until zChunks).forEach { zChunk ->
              updateBlockMesh(world, Vector3i(xChunk, yChunk, zChunk))
            }
          }
        }
      }
      is UpdateBlock -> {
        val world = this.world!!
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
        val world = this.world!!
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
        val world = this.world!!
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
        val world = this.world!!
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

  lateinit private var listener: MessageChannel
  private val skyLightDirection = Vector3(0f, -1f, -0.5f).normalize()
  lateinit private var skyLightLevels: LightLevels
  lateinit private var blockLightLevels: LightLevels
  override fun willMount() = runBlocking<Unit> {
    program = ColorShaderProgram()
    skyLightLevels = LightLevels.load(loadByteBuffer("SkyLightLevels.png", WorldRenderer::class.java), 240)
    blockLightLevels = LightLevels.load(loadByteBuffer("BlockLightLevels.png", WorldRenderer::class.java), 16)
    listener = register(channel)
    controller.register()
    MessageBus.send(NewGameRequested(Cause.of(this), Context()))
  }

  override fun willUnmount() {
    unregister(listener)
    controller.unregister()
  }

  override fun render(bounds: Bounds) {
    super.render(bounds)

    val world = world

    controller.viewCenter.y = if (world != null) {
      val yRange = (0 until sliceHeight)
      val viewHeight = heightAt(world, controller.view.x.toInt(), controller.view.z.toInt(), yRange).toFloat()
      val viewCenterHeight = heightAt(world, controller.viewCenter.x.toInt(), controller.viewCenter.z.toInt(), yRange).toFloat()
      val minimumHeight = viewHeight + 5f
      val desiredHeight = viewCenterHeight + controller.viewOffset.y

      Math.max(minimumHeight, desiredHeight) - controller.viewOffset.y
    } else {
      0f
    }

    val variables = Context(
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