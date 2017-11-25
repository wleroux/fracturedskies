package com.fracturedskies.render.components.world

import com.fracturedskies.Contexts.UI_CONTEXT
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.event.EventHandlers
import com.fracturedskies.engine.jeact.event.on
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.engine.math.Vector3
import com.fracturedskies.engine.math.Vector4
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.unregister
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.game.BlockType
import com.fracturedskies.game.World
import com.fracturedskies.game.messages.NewGameRequested
import com.fracturedskies.game.messages.WorldGenerated
import com.fracturedskies.game.rayCast
import com.fracturedskies.render.events.Click
import com.fracturedskies.render.events.Key
import com.fracturedskies.render.events.Unfocus
import com.fracturedskies.render.shaders.*
import com.fracturedskies.render.shaders.noop.NoopProgram
import com.fracturedskies.render.shaders.noop.NoopProgram.Companion.ALBEDO
import com.fracturedskies.render.shaders.standard.StandardShaderProgram
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0
import org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT

class WorldRenderer(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    fun Node.Builder<*>.worldRenderer(additionalContext: Context = Context()) {
      nodes.add(Node(::WorldRenderer, additionalContext))
    }
  }

  private val controller = Controller()
  override val handler = EventHandlers(on(Key::class) { key ->
    if (key.action == GLFW.GLFW_PRESS) {
      controller.press(key.key)
    } else if (key.action == GLFW.GLFW_RELEASE){
      controller.release(key.key)
    }
  }, on(Unfocus::class) {
    controller.clear()
  }, on(Click::class) { event->
    if (event.action != GLFW.GLFW_PRESS) {
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

    val blockRay = rayCast(world!!, rayStart3, direction)
    blockRay.filter { it.block.type != BlockType.AIR }.forEach {
      it.block.type = BlockType.AIR
    }
    runBlocking {
      MessageBus.dispatch(WorldGenerated(world!!, Cause.of(this), Context()))
    }
  })

  lateinit private var material: Material
  lateinit private var variables: Context
  private var world: World? = null
  private var worldMesh: Mesh? = null
  lateinit private var listener: MessageChannel
  private var renderMeshJob: Job? = null

  override fun willMount() = runBlocking {
    material = Material(
            StandardShaderProgram(),
            Context(StandardShaderProgram.ALBEDO to TextureArray("tileset.png", loadByteBuffer("tileset.png", this@WorldRenderer.javaClass), 16, 16, 3))
    )

    listener = register(MessageChannel(UI_CONTEXT) { message ->
      when (message) {
        is WorldGenerated -> {
          world = message.world
          renderMeshJob?.cancel()
          renderMeshJob = launch {
            val worldMeshGenerator = WorldMeshGenerator()
                    .generateMesh(message.world, 0 until message.world.width, 0 until message.world.height, 0 until message.world.depth)
            launch(coroutineContext + UI_CONTEXT) {
              worldMesh = worldMeshGenerator()
            }
          }
        }
      }
    })
    controller.register()

    MessageBus.dispatch(NewGameRequested(Cause.of(this), Context()))

    framebuffer = Framebuffer()
    framebuffer.drawBuffers(GL_COLOR_ATTACHMENT0)
    renderedTextureMesh = Mesh(floatArrayOf(
            -1f,  1f, 0f,  0f, 1f, 0f,
            1f,  1f, 0f,  1f, 1f, 0f,
            1f, -1f, 0f,  1f, 0f, 0f,
            -1f, -1f, 0f,  0f, 0f, 0f
    ), intArrayOf(
            0, 1, 2,
            2, 3, 0
    ), listOf(Mesh.Attribute.POSITION, Mesh.Attribute.TEXCOORD));

  }
  lateinit var renderedTextureMesh: Mesh
  lateinit var framebuffer: Framebuffer

  override fun willUnmount() {
    unregister(listener)
    controller.unregister()
  }

  override fun render(bounds: Bounds) {
    super.render(bounds)
    val mesh = worldMesh
    if (mesh != null) {
      variables = Context(
              StandardShaderProgram.MODEL to Matrix4(Vector3(0f, 0f, 0f)),
              StandardShaderProgram.VIEW to Matrix4(controller.view, controller.rotation).invert(),
              StandardShaderProgram.PROJECTION to Matrix4.perspective(Math.PI.toFloat() / 4, bounds.width, bounds.height, 0.03f, 1000f)
      )

      // Render Texture
      val renderedTexture = Texture("renderedTexture", null, bounds.width, bounds.height)
      val depthRenderbuffer = Renderbuffer(bounds.width, bounds.height, GL_DEPTH_COMPONENT)
      framebuffer.renderbuffer(GL_DEPTH_ATTACHMENT, depthRenderbuffer)
      framebuffer.texture(GL_COLOR_ATTACHMENT0, renderedTexture)
      framebuffer.bind {
        glViewport(0, 0, bounds.width, bounds.height)
        glClear(GL_DEPTH_BUFFER_BIT)
        material.render(variables, mesh)
      }

      // Render to window instead!
      glViewport(bounds.x, bounds.y, bounds.width, bounds.height)
      val program = NoopProgram()

      Material(program, Context()).render(Context(
              ALBEDO to renderedTexture
      ), renderedTextureMesh)

      depthRenderbuffer.close()
      renderedTexture.close()
      program.close()
    }
  }
}
