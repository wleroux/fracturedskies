package com.fracturedskies.render.shaders.color

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.shaders.Mesh
import com.fracturedskies.render.shaders.ShaderProgram
import org.lwjgl.opengl.GL20.glUseProgram

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
    bind {
      model(requireNotNull(variables[MODEL]))
      view(requireNotNull(variables[VIEW]))
      projection(requireNotNull(variables[PROJECTION]))

      // Render
      draw(mesh)
    }
  }

  inline fun bind(block: ColorShaderProgram.()->Unit) {
    glUseProgram(id)
    this.block()
    glUseProgram(0)
  }

  fun model(model: Matrix4) {
    uniform(MODEL_LOCATION, model)
  }

  fun view(view: Matrix4) {
    uniform(VIEW_LOCATION, view)
  }

  fun projection(projection: Matrix4) {
    uniform(PROJECTION_LOCATION, projection)
  }
}
