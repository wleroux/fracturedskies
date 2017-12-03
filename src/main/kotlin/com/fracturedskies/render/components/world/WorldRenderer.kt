package com.fracturedskies.render.components.world

import com.fracturedskies.UI_CONTEXT
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.event.EventHandlers
import com.fracturedskies.engine.jeact.event.on
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.unregister
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.game.BlockType
import com.fracturedskies.game.Game
import com.fracturedskies.game.World
import com.fracturedskies.game.messages.*
import com.fracturedskies.game.raycast
import com.fracturedskies.render.events.*
import com.fracturedskies.render.shaders.Mesh
import com.fracturedskies.render.shaders.color.ColorShaderProgram
import com.fracturedskies.render.shaders.standard.StandardShaderProgram
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11.*
import java.lang.Integer.max
import java.lang.Integer.min

class WorldRenderer(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    val CHUNK_X_SIZE = 32
    val CHUNK_Y_SIZE = 128
    val CHUNK_Z_SIZE = 32
    val CHUNKS = Vector3i(CHUNK_X_SIZE, CHUNK_Y_SIZE, CHUNK_Z_SIZE)
    fun Node.Builder<*>.worldRenderer(additionalContext: Context = Context()) {
      nodes.add(Node(::WorldRenderer, additionalContext))
    }
  }

  private var firstBlock: Vector3i? = null
  private var focused = false
  private val controller = Controller()
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

    val selectedBlock = raycast(game.world!!, rayStart3, direction)
            .filter { it.block.type != BlockType.AIR }
            .firstOrNull()
    if (selectedBlock == null) {
      firstBlock = null
    } else {
      when (event.action) {
        GLFW.GLFW_PRESS -> {
          firstBlock = if (event.button == GLFW.GLFW_MOUSE_BUTTON_1) {
            selectedBlock.position
          } else {
            selectedBlock.position + selectedBlock.faces.first()
          }
        }
        GLFW.GLFW_RELEASE -> {
          if (firstBlock != null) {
            val secondBlock = if (event.button == GLFW.GLFW_MOUSE_BUTTON_1) {
              selectedBlock.position
            } else {
              selectedBlock.position + selectedBlock.faces.first()
            }

            val xRange = min(firstBlock!!.x, secondBlock.x)..max(firstBlock!!.x, secondBlock.x)
            val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
            val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)
            xRange.forEach { x ->
              zRange.forEach { z ->
                yRange.forEach { y ->
                  if (game.world!!.has(x, y, z)) {
                    if (event.button == GLFW.GLFW_MOUSE_BUTTON_1) {
                      if (game.world!![x, y, z].type != BlockType.AIR) {
                        MessageBus.send(QueueWork(UpdateBlockWork(x, y, z, BlockType.AIR, WorkType.CONSTRUCTION, 0), Cause.of(this), Context()))
                      }
                    } else {
                      if (game.world!![x, y, z].type != BlockType.BLOCK) {
                        MessageBus.send(QueueWork(UpdateBlockWork(x, y, z, BlockType.BLOCK, WorkType.CONSTRUCTION, 0), Cause.of(this), Context()))
                      }
                    }
                  }
                }
              }
            }
          }
          firstBlock = null
        }
      }
    }
  })

  lateinit private var program: ColorShaderProgram
  private var game = Game(coroutineContext = UI_CONTEXT) { message ->
    when (message) {
      is UpdateBlock -> {
        val world = this.world!!
        (Vector3i.NEIGHBOURS + Vector3i.ADDITIVE_UNIT)
                .map { message.pos + it }
                .map { it / CHUNKS }
                .filter {
                  it.x in (0 until world.width / CHUNK_X_SIZE) &&
                          it.y in (0 until world.height / CHUNK_Y_SIZE) &&
                          it.z in (0 until world.depth / CHUNK_Z_SIZE)
                }
                .distinct()
                .forEach { updateChunk(world, it)}
      }
      is LightUpdated -> {
        message.updates
                .map { (pos, _) -> pos / CHUNKS }
                .distinct()
                .forEach { updateChunk(world!!, it) }
      }
      is WorldGenerated -> {
        val world = this.world!!
        val xChunks = world.width / CHUNK_X_SIZE
        val yChunks = world.height / CHUNK_Y_SIZE
        val zChunks = world.depth / CHUNK_Z_SIZE
        (0 until xChunks).forEach { xChunk ->
          (0 until yChunks).forEach { yChunk ->
            (0 until zChunks).forEach { zChunk ->
              updateChunk(world, Vector3i(xChunk, yChunk, zChunk))
            }
          }
        }
      }
    }
  }

  private fun updateChunk(world: World, chunk: Vector3i) {
    renderMeshJob[chunk]?.cancel()
    renderMeshJob[chunk] = launch {
      val meshGenerator = generateWorldMesh(world,
              (chunk.x * CHUNK_X_SIZE) until ((chunk.x + 1) * CHUNK_X_SIZE),
              (chunk.y * CHUNK_Y_SIZE) until ((chunk.y + 1) * CHUNK_Y_SIZE),
              (chunk.z * CHUNK_Z_SIZE) until ((chunk.z + 1) * CHUNK_Z_SIZE))
      launch(coroutineContext + UI_CONTEXT) {
        if (isActive) {
          worldMesh[chunk]?.close()
          worldMesh[chunk] = meshGenerator()
        }
      }
    }
  }

  private var worldMesh = mutableMapOf<Vector3i, Mesh>()
  lateinit private var listener: MessageChannel
  private var renderMeshJob = mutableMapOf<Vector3i, Job>()

  override fun willMount() = runBlocking<Unit> {
    program = ColorShaderProgram()
    listener = register(game.channel)
    controller.register()
    MessageBus.send(NewGameRequested(Cause.of(this), Context()))
  }

  override fun willUnmount() {
    unregister(listener)
    controller.unregister()
  }

  override fun render(bounds: Bounds) {
    super.render(bounds)

    val world = game.world

    controller.viewCenter.y = if (world != null) {
      val viewHeight = heightAt(world, controller.view.x.toInt(), controller.view.z.toInt()).toFloat()
      val viewCenterHeight = heightAt(world, controller.viewCenter.x.toInt(), controller.viewCenter.z.toInt()).toFloat()
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
      worldMesh.forEach { _, mesh ->
        draw(mesh)
      }
    }
  }

  private fun heightAt(world: World, x: Int, z: Int): Int {
    val clampedX = clamp(x, 0 until world.width)
    val clampedZ = clamp(z, 0 until world.depth)
    return (0 until world.height)
            .reversed()
            .firstOrNull { world[clampedX, it, clampedZ].type != BlockType.AIR }
            ?: 0
  }
}