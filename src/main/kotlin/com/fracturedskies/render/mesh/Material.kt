package com.fracturedskies.render.mesh

data class Material<in P, in V>(private val program: ShaderProgram<P, V>, private val properties: P) {
  fun render(variables: V, mesh: Mesh) {
    program.render(properties, variables, mesh)
  }
}