package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds
import com.fracturedskies.render.mesh.Material
import com.fracturedskies.render.mesh.Mesh
import org.lwjgl.opengl.GL11

class MeshRenderer(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    val MESH = Key<Mesh>("mesh")
    val MATERIAL = Key<Material>("material")
    val VARIABLES = Key<Context>("variables")
  }

  /** Attributes */
  val mesh get() = requireNotNull(attributes[MESH])
  val material get() = requireNotNull(attributes[MATERIAL])
  val variables get() = requireNotNull(attributes[VARIABLES])

  override fun render(bounds: Bounds) {
    this.bounds = bounds
    GL11.glViewport(bounds.x, bounds.y, bounds.width, bounds.height)
    material.render(variables, mesh)
  }
}
