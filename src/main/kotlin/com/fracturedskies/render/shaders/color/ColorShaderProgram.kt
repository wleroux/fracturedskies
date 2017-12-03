package com.fracturedskies.render.shaders.color

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.shaders.Mesh
import com.fracturedskies.render.shaders.ShaderProgram
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

class ColorShaderProgram : ShaderProgram(this::class.java.getResource("color.vert").readText(), this::class.java.getResource("color.frag").readText()) {
  companion object {
    /**
     * Variables
     */
    val MODEL = Key<Matrix4>("model")
    val VIEW = Key<Matrix4>("view")
    val PROJECTION = Key<Matrix4>("projection")

    /**
     * Standard Shader Uniforms
     */
    private val MODEL_LOCATION = 0
    private val VIEW_LOCATION = 1
    private val PROJECTION_LOCATION = 2
  }

  override fun render(properties: Context, variables: Context, mesh: Mesh) {
    // Shader Configuration
    GL20.glUseProgram(id)
    GL11.glEnable(GL11.GL_DEPTH_TEST)

    // Variables
    uniform(MODEL_LOCATION, requireNotNull(variables[MODEL]))
    uniform(VIEW_LOCATION, requireNotNull(variables[VIEW]))
    uniform(PROJECTION_LOCATION, requireNotNull(variables[PROJECTION]))

    // Render
    draw(mesh)
  }
}
