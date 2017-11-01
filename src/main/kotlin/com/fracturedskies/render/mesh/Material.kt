package com.fracturedskies.render.mesh

import com.fracturedskies.engine.collections.TypedMap

data class Material(private val program: ShaderProgram, private val properties: TypedMap) {
  fun render(variables: TypedMap, mesh: Mesh) {
    program.render(properties, variables, mesh)
  }
}