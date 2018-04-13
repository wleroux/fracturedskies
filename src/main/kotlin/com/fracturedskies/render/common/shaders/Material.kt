package com.fracturedskies.render.common.shaders

import com.fracturedskies.engine.collections.MultiTypeMap

data class Material(val program: ShaderProgram, private val properties: MultiTypeMap) {
  fun render(variables: MultiTypeMap, mesh: Mesh) {
    program.render(properties, variables, mesh)
  }
}