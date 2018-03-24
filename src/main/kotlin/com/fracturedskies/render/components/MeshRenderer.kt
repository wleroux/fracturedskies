package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.render.shaders.*
import org.lwjgl.opengl.GL11

class MeshRenderer(attributes: MultiTypeMap) : Component<Unit>(attributes, Unit) {
  companion object {
    val MESH = TypedKey<Mesh>("mesh")
    val MATERIAL = TypedKey<Material>("material")
    val VARIABLES = TypedKey<MultiTypeMap>("variables")
    fun Node.Builder<*>.meshRenderer(mesh: Mesh, material: Material, variables: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::MeshRenderer, MultiTypeMap(
              MESH to mesh,
              MATERIAL to material,
              VARIABLES to variables
      )))
    }
  }

  /** Attributes */
  private val mesh get() = requireNotNull(props[MESH])
  private val material get() = requireNotNull(props[MATERIAL])
  private val variables get() = requireNotNull(props[VARIABLES])

  override fun render(bounds: Bounds) {
    this.bounds = bounds
    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT)
    GL11.glViewport(bounds.x, bounds.y, bounds.width, bounds.height)
    material.render(variables, mesh)
  }
}
