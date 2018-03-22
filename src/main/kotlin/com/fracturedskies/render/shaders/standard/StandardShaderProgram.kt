package com.fracturedskies.render.shaders.standard

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.shaders.*
import org.lwjgl.opengl.*

class StandardShaderProgram: ShaderProgram(this::class.java.getResource("standard.vert").readText(), this::class.java.getResource("standard.frag").readText()) {
  companion object {
    /**
     * Properties
     */
    val ALBEDO = TypedKey<TextureArray>("albedo")

    /**
     * Variables
     */
    val MODEL = TypedKey<Matrix4>("model")
    val VIEW = TypedKey<Matrix4>("view")
    val PROJECTION = TypedKey<Matrix4>("projection")

    /**
     * Standard Shader Uniforms
     */
    private const val MODEL_LOCATION = 0
    private const val VIEW_LOCATION = 1
    private const val PROJECTION_LOCATION = 2
    private const val ALBEDO_LOCATION = 3
  }

  override fun render(properties: MultiTypeMap, variables: MultiTypeMap, mesh: Mesh) {
    // Shader Configuration
    GL20.glUseProgram(id)
    GL11.glEnable(GL11.GL_DEPTH_TEST)

    // Material Properties
    uniform(ALBEDO_LOCATION, GL13.GL_TEXTURE0, requireNotNull(properties[ALBEDO]))

    // Variables
    uniform(MODEL_LOCATION, requireNotNull(variables[MODEL]))
    uniform(VIEW_LOCATION, requireNotNull(variables[VIEW]))
    uniform(PROJECTION_LOCATION, requireNotNull(variables[PROJECTION]))

    // Render
    draw(mesh)
  }
}
