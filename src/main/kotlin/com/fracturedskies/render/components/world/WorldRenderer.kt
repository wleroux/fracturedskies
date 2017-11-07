package com.fracturedskies.render.components.world

import com.fracturedskies.Contexts.UI_CONTEXT
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.engine.math.Quaternion4
import com.fracturedskies.engine.math.Vector3
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.unregister
import com.fracturedskies.engine.messages.MessageChannel
import com.fracturedskies.game.World
import com.fracturedskies.game.messages.NewGameRequested
import com.fracturedskies.game.messages.WorldGenerated
import com.fracturedskies.render.mesh.Material
import com.fracturedskies.render.mesh.Mesh
import com.fracturedskies.render.mesh.TextureArray
import com.fracturedskies.render.mesh.standard.StandardShaderProgram
import kotlinx.coroutines.experimental.runBlocking
import org.lwjgl.opengl.GL11.glViewport
import java.lang.Math.PI

class WorldRenderer(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    fun Node.Builder<*>.worldRenderer(additionalContext: Context = Context()) {
      nodes.add(Node(::WorldRenderer, additionalContext))
    }
  }

  lateinit private var material: Material
  lateinit private var variables: Context
  private var world: World? = null
  private var worldMesh: Mesh? = null
  lateinit private var listener: MessageChannel

  override fun willMount() = runBlocking {
    material = Material(
            StandardShaderProgram(),
            Context(StandardShaderProgram.ALBEDO to TextureArray("tileset.png", loadByteBuffer("com/fracturedskies/render/tileset.png", this.javaClass.classLoader), 16, 16, 3))
    )

    listener = register(MessageChannel(UI_CONTEXT) { message ->
      when (message) {
        is WorldGenerated -> {
          world = message.world
          worldMesh = WorldMeshGenerator()
                  .generateMesh(message.world, 0 until message.world.width, 0 until message.world.height, 0 until message.world.depth)
        }
      }
    })
    MessageBus.dispatch(NewGameRequested(Cause.of(this), Context()))
  }

  override fun willUnmount() {
    unregister(listener)
  }

  override fun render(bounds: Bounds) {
    super.render(bounds)
    val mesh = worldMesh
    if (mesh != null) {
      variables = Context(
              StandardShaderProgram.MODEL to Matrix4(Vector3(0f, 0f, 10f)),
              StandardShaderProgram.VIEW to Matrix4(
                      Vector3(64f, 300f, -30f),
                      Quaternion4(Vector3.AXIS_X, (PI / 3).toFloat())).invert(),
              StandardShaderProgram.PROJECTION to Matrix4.perspective(
                      Math.PI.toFloat() / 4,
                      bounds.width,
                      bounds.height,
                      0.03f,
                      1000f
              )
      )
      glViewport(bounds.x, bounds.y, bounds.width, bounds.height)
      material.render(variables, mesh)
    }
  }
}
