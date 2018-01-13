package com.fracturedskies.game.render.shaders.standard

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.game.render.shaders.Mesh
import com.fracturedskies.game.render.shaders.ShaderProgram
import com.fracturedskies.game.render.shaders.TextureArray
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20

class StandardShaderProgram: ShaderProgram(this::class.java.getResource("standard.vert").readText(), this::class.java.getResource("standard.frag").readText()) {
  companion object {
    /**
     * Properties
     */
    val ALBEDO = Key<TextureArray>("albedo")

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
    private val ALBEDO_LOCATION = 3
  }

  override fun render(properties: Context, variables: Context, mesh: Mesh) {
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
