package com.fracturedskies.render.shaders.noop

import com.fracturedskies.engine.collections.*
import com.fracturedskies.render.shaders.*
import org.lwjgl.opengl.*

class NoopProgram : ShaderProgram(this::class.java.getResource("noop.vert").readText(), this::class.java.getResource("noop.frag").readText()) {
  companion object {
    /**
     * Variables
     */
    val ALBEDO = TypedKey<Texture>("albedo")

    /**
     * Standard Shader Uniforms
     */
    private val ALBEDO_LOCATION = 3
  }

  override fun render(properties: MultiTypeMap, variables: MultiTypeMap, mesh: Mesh) {
    // Shader Configuration
    GL20.glUseProgram(id)
    GL11.glEnable(GL11.GL_DEPTH_TEST)

    // Material Properties
    uniform(ALBEDO_LOCATION, GL13.GL_TEXTURE0, requireNotNull(variables[ALBEDO]))

    // Render
    draw(mesh)
  }
}
