package com.fracturedskies.render.shaders

import com.fracturedskies.engine.collections.Context

data class Material(private val program: ShaderProgram, private val properties: Context) {
  fun render(variables: Context, mesh: Mesh) {
    program.render(properties, variables, mesh)
  }
}