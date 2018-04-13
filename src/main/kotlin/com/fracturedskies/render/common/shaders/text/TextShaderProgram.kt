package com.fracturedskies.render.common.shaders.text

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.common.shaders.*
import org.lwjgl.opengl.*

class TextShaderProgram: ShaderProgram(this::class.java.getResource("text.vert").readText(), this::class.java.getResource("text.frag").readText()) {
  companion object {
    /**
     * Variables
     */
    val PROJECTION = TypedKey<Matrix4>("projection")

    /**
     * Standard Shader Uniforms
     */
    const val PROJECTION_LOCATION = 2
  }

  override fun render(properties: MultiTypeMap, variables: MultiTypeMap, mesh: Mesh) {
    // Shader Configuration
    GL20.glUseProgram(id)
    GL11.glEnable(GL11.GL_DEPTH_TEST)

    // Uniform
    uniform(PROJECTION_LOCATION, requireNotNull(variables[PROJECTION]))

    // Render
    draw(mesh)
  }

}