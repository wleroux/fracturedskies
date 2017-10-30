package com.fracturedskies.render.mesh.standard

import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.mesh.Mesh
import com.fracturedskies.render.mesh.ShaderProgram
import com.fracturedskies.render.mesh.standard.StandardShaderProgram.Properties
import com.fracturedskies.render.mesh.standard.StandardShaderProgram.Variables
import com.fracturedskies.render.mesh.TextureArray
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20

class StandardShaderProgram: ShaderProgram<Properties, Variables>(this::class.java.getResource("standard.vert").readText(), this::class.java.getResource("standard.frag").readText()) {
  data class Properties(val albedo: TextureArray)
  data class Variables(val model: Matrix4, val view: Matrix4, val projection: Matrix4)

  /**
   * Standard Shader Uniforms
   */
  companion object {
    private val MODEL_LOCATION = 0
    private val VIEW_LOCATION = 1
    private val PROJECTION_LOCATION = 2
    private val ALBEDO_LOCATION = 3
  }

  override fun render(properties: Properties, variables: Variables, mesh: Mesh) {
    // Shader Configuration
    GL20.glUseProgram(id)
    GL11.glEnable(GL11.GL_DEPTH_TEST)

    // Material Properties
    uniform(ALBEDO_LOCATION, GL13.GL_TEXTURE0, properties.albedo)

    // Variables
    uniform(MODEL_LOCATION, variables.model)
    uniform(VIEW_LOCATION, variables.view)
    uniform(PROJECTION_LOCATION, variables.projection)

    // Render
    draw(mesh)
  }
}
