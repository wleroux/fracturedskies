package com.fracturedskies.render.shaders.noop

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.render.shaders.Mesh
import com.fracturedskies.render.shaders.ShaderProgram
import com.fracturedskies.render.shaders.Texture
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20

class NoopProgram : ShaderProgram(this::class.java.getResource("noop.vert").readText(), this::class.java.getResource("noop.frag").readText()) {
  companion object {
    /**
     * Variables
     */
    val ALBEDO = Key<Texture>("albedo")

    /**
     * Standard Shader Uniforms
     */
    private val ALBEDO_LOCATION = 3
  }

  override fun render(properties: Context, variables: Context, mesh: Mesh) {
    // Shader Configuration
    GL20.glUseProgram(id)
    GL11.glEnable(GL11.GL_DEPTH_TEST)

    // Material Properties
    uniform(ALBEDO_LOCATION, GL13.GL_TEXTURE0, requireNotNull(variables[ALBEDO]))

    // Render
    draw(mesh)
  }
}
