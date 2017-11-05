package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.mesh.Material
import com.fracturedskies.render.mesh.Mesh
import com.fracturedskies.render.mesh.standard.StandardShaderProgram
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11


class BorderImage(attributes: Context) : AbstractComponent<Boolean>(attributes, false) {
  companion object {
    val MATERIAL = Key<Material>("material")
    private val LAYER_WIDTH = 10
    private val LAYER_HEIGHT = 10
    fun Node.Builder<*>.borderImage(material: Material, additionalContext: Context = Context(), block: Node.Builder<*>.()->Unit = {}) {
      nodes.add(Node(::BorderImage, Context(
              MATERIAL to material
      ).with(additionalContext), block))
    }
  }
  override fun preferredWidth(parentWidth: Int, parentHeight: Int) =
          2 * LAYER_WIDTH + super.preferredWidth(parentWidth - 2 * LAYER_WIDTH, parentHeight - 2 * LAYER_HEIGHT)
  override fun preferredHeight(parentWidth: Int, parentHeight: Int) =
          2 * LAYER_HEIGHT + super.preferredHeight(parentWidth - 2 * LAYER_WIDTH, parentHeight - 2 * LAYER_HEIGHT)

  /* Attributes */
  private val material: Material get() = requireNotNull(attributes[MATERIAL])

  override fun render(bounds: Bounds) {
    this.bounds = bounds
    val fillWidth = bounds.width - 2 * LAYER_WIDTH
    val fillHeight = bounds.height - 2 * LAYER_HEIGHT

    // @formatter:off
    val verticesBuffer = BufferUtils.createFloatBuffer(9 * 4 * 9)
    val indicesBuffer = BufferUtils.createIntBuffer(6 * 9)
    var vertexCount = 0
    for (i in 0..2) {
      for (j in 0..2) {
        val tile = j * 3 + i
        val xOffset = if (i != 2) if (i == 0) 0 else LAYER_WIDTH else LAYER_WIDTH + fillWidth
        val yOffset = if (j != 2) if (j == 0) LAYER_HEIGHT + fillHeight else LAYER_HEIGHT else 0
        val w = if (i != 2) if (i == 0) LAYER_WIDTH else fillWidth else LAYER_WIDTH
        val h = if (j != 2) if (j == 0) LAYER_HEIGHT else fillHeight else LAYER_HEIGHT

        verticesBuffer.put(floatArrayOf(
                (xOffset + 0).toFloat(), (yOffset + h).toFloat(), 0f, 0f, 0f, tile.toFloat(), 0f, 0f, 1f,
                (xOffset + w).toFloat(), (yOffset + h).toFloat(), 0f, 1f, 0f, tile.toFloat(), 0f, 0f, 1f,
                (xOffset + w).toFloat(), (yOffset + 0).toFloat(), 0f, 1f, 1f, tile.toFloat(), 0f, 0f, 1f,
                (xOffset + 0).toFloat(), (yOffset + 0).toFloat(), 0f, 0f, 1f, tile.toFloat(), 0f, 0f, 1f
        ))
        indicesBuffer.put(intArrayOf(
                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                vertexCount + 2, vertexCount + 3, vertexCount + 0
        ))
        vertexCount += 4
      }
    }
    // @formatter:on
    verticesBuffer.flip()
    val vertices = FloatArray(verticesBuffer.remaining())
    verticesBuffer.get(vertices)

    indicesBuffer.flip()
    val indices = IntArray(indicesBuffer.remaining())
    indicesBuffer.get(indices)

    val mesh = Mesh(vertices, indices, listOf(Mesh.Attribute.POSITION, Mesh.Attribute.TEXCOORD, Mesh.Attribute.NORMAL))
    val variables = Context(
            StandardShaderProgram.MODEL to Matrix4.IDENTITY,
            StandardShaderProgram.VIEW to Matrix4.IDENTITY,
            StandardShaderProgram.PROJECTION to Matrix4.orthogonal(0f, bounds.width.toFloat(), 0f, bounds.height.toFloat(), -1f, 1000f)
    )

    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT)
    GL11.glViewport(bounds.x, bounds.y, bounds.width, bounds.height)
    material.render(variables, mesh)
    for (child in children) {
      child.render(Bounds(bounds.x + LAYER_WIDTH, bounds.y + LAYER_HEIGHT, bounds.width - 2 * LAYER_WIDTH, bounds.height - 2 * LAYER_HEIGHT))
    }
  }
}