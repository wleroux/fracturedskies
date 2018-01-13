package com.fracturedskies.game.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.game.render.shaders.Material
import com.fracturedskies.game.render.shaders.Mesh
import org.lwjgl.opengl.GL11

class MeshRenderer(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    val MESH = Key<Mesh>("mesh")
    val MATERIAL = Key<Material>("material")
    val VARIABLES = Key<Context>("variables")
    fun Node.Builder<*>.meshRenderer(mesh: Mesh, material: Material, variables: Context = Context()) {
      nodes.add(Node(::MeshRenderer, Context(
              MESH to mesh,
              MATERIAL to material,
              VARIABLES to variables
      )))
    }
  }

  /** Attributes */
  private val mesh get() = requireNotNull(attributes[MESH])
  private val material get() = requireNotNull(attributes[MATERIAL])
  private val variables get() = requireNotNull(attributes[VARIABLES])

  override fun render(bounds: Bounds) {
    this.bounds = bounds
    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT)
    GL11.glViewport(bounds.x, bounds.y, bounds.width, bounds.height)
    material.render(variables, mesh)
  }
}