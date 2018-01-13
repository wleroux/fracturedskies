package com.fracturedskies.game.render.shaders.text

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.game.render.shaders.Mesh
import com.fracturedskies.game.render.shaders.ShaderProgram
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

class TextShaderProgram: ShaderProgram(this::class.java.getResource("text.vert").readText(), this::class.java.getResource("text.frag").readText()) {
  companion object {
    /**
     * Variables
     */
    val PROJECTION = Key<Matrix4>("projection")

    /**
     * Standard Shader Uniforms
     */
    private val PROJECTION_LOCATION = 2
  }

  override fun render(properties: Context, variables: Context, mesh: Mesh) {
    // Shader Configuration
    GL20.glUseProgram(id)
    GL11.glEnable(GL11.GL_DEPTH_TEST)

    // Uniform
    uniform(PROJECTION_LOCATION, requireNotNull(variables[PROJECTION]))

    // Render
    draw(mesh)
  }

}