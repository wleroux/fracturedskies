package com.fracturedskies.render.mesh.standard

import com.fracturedskies.engine.collections.TypedKey
import com.fracturedskies.engine.collections.TypedMap
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.mesh.Mesh
import com.fracturedskies.render.mesh.ShaderProgram
import com.fracturedskies.render.mesh.TextureArray
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20

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
    private val MODEL_LOCATION = 0
    private val VIEW_LOCATION = 1
    private val PROJECTION_LOCATION = 2
    private val ALBEDO_LOCATION = 3
  }

  override fun render(properties: TypedMap, variables: TypedMap, mesh: Mesh) {
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
