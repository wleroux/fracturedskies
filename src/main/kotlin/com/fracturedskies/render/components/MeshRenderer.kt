package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.TypedKey
import com.fracturedskies.engine.collections.TypedMap
import com.fracturedskies.engine.jeact.Component
import com.fracturedskies.render.mesh.Material
import com.fracturedskies.render.mesh.Mesh

class MeshRenderer(override var attributes: TypedMap) : Component, Renderable {
  companion object {
    val MESH = TypedKey<Mesh>("mesh")
    val MATERIAL = TypedKey<Material>("material")
    val VARIABLES = TypedKey<TypedMap>("variables")
  }

  override var state: Any? = null
  override var nextState: Any? = null

  val mesh: Mesh
    get() = requireNotNull(attributes[MESH])
  val material: Material
    get() = requireNotNull(attributes[MATERIAL])
  val variables: TypedMap
    get() = requireNotNull(attributes[VARIABLES])

  override fun render() {
    material.render(variables, mesh)
  }
}
