package com.fracturedskies.render.shaders.color

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.components.world.LightLevels
import com.fracturedskies.render.shaders.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glUniform4uiv

class ColorShaderProgram : ShaderProgram(this::class.java.getResource("color.vert").readText(), this::class.java.getResource("color.frag").readText()) {
  companion object {
    /**
     * Variables
     */
    val MODEL = TypedKey<Matrix4>("model")
    val VIEW = TypedKey<Matrix4>("view")
    val PROJECTION = TypedKey<Matrix4>("projection")

    /**
     * Standard Shader Uniforms
     */
    private val MODEL_LOCATION = 0
    private val VIEW_LOCATION = 1
    private val PROJECTION_LOCATION = 2

    private val LIGHT_DIRECTION_LOCATION = 3
    private val SKY_COLORS_LOCATION = 4
    private val BLOCK_COLORS_LOCATION = 20
  }

  override fun render(properties: MultiTypeMap, variables: MultiTypeMap, mesh: Mesh) {
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

  fun skyColors(skyLightLevels: LightLevels, timeOfDay: Float) {
    glUniform4uiv(SKY_COLORS_LOCATION, skyLightLevels.getColorBuffer(timeOfDay))
  }

  fun blockColors(blockLightLevels: LightLevels) {
    glUniform4uiv(BLOCK_COLORS_LOCATION, blockLightLevels.getColorBuffer(0.5f))
  }

  fun lightDirection(dir: Vector3) {
    uniform(LIGHT_DIRECTION_LOCATION, dir)
  }

  private fun uniform(location: Int, value: Vector3) {
    glUniform3f(location, value.x, value.y, value.z)
  }
}
