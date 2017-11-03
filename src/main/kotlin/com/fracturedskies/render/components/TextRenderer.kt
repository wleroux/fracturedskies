package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.math.Color4
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.components.MeshRenderer.Companion.MATERIAL
import com.fracturedskies.render.components.MeshRenderer.Companion.MESH
import com.fracturedskies.render.components.MeshRenderer.Companion.VARIABLES
import com.fracturedskies.render.mesh.Material
import com.fracturedskies.render.mesh.Mesh
import com.fracturedskies.render.mesh.Mesh.Attribute.Companion.POSITION
import com.fracturedskies.render.mesh.text.Text
import com.fracturedskies.render.mesh.text.TextShaderProgram
import com.fracturedskies.render.mesh.text.TextShaderProgram.Companion.PROJECTION
import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBEasyFont.*

class TextRenderer(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    val TEXT = Key<Text>("text")
    val COLOR = Key<Color4>("color")
  }
  private val text get() = requireNotNull(attributes[TEXT])
  private val color get() = requireNotNull(attributes[COLOR])

  override fun preferredWidth() = stb_easy_font_width(text.value)
  override fun preferredHeight() = stb_easy_font_height(text.value)

  override fun toNode(): List<Node<*>> {
    val colorBuffer = BufferUtils.createByteBuffer(4)
    colorBuffer.put(color.red.toByte())
    colorBuffer.put(color.green.toByte())
    colorBuffer.put(color.blue.toByte())
    colorBuffer.put(color.alpha.toByte())
    colorBuffer.flip()

    val buffer = BufferUtils.createByteBuffer(999990)
    val numQuads = stb_easy_font_print(0f, 0f, text.value, colorBuffer, buffer)
    val floatBuffer = buffer.asFloatBuffer()

    val vertices = FloatArray(numQuads * 4 * 4)
    val indices = IntArray(numQuads * 6)
    for (quad in 0 until numQuads) {
      for (i in 0 until 4 * 4) {
        vertices[quad * 4 * 4 + i] = floatBuffer[quad * 4 * 4 + i]
      }
      indices[(quad * 6) + 0] = (quad * 4) + 0
      indices[(quad * 6) + 1] = (quad * 4) + 1
      indices[(quad * 6) + 2] = (quad * 4) + 2
      indices[(quad * 6) + 3] = (quad * 4) + 2
      indices[(quad * 6) + 4] = (quad * 4) + 3
      indices[(quad * 6) + 5] = (quad * 4) + 0
    }
    val material = Material(TextShaderProgram(), Context())
    val mesh = Mesh(
            vertices,
            indices,
            listOf(POSITION, Mesh.Attribute.COLOR)
    )
    val projection = Matrix4.orthogonal(0f, preferredWidth().toFloat(), preferredHeight().toFloat(), 0f, -1f, 1000f)

    return listOf(Node(::MeshRenderer, Context(
            MESH to mesh,
            MATERIAL to material,
            VARIABLES to Context(
                    PROJECTION to projection
            )
    )))
  }

  override fun render(bounds: Bounds) {
    this.bounds = bounds
    for (child in children) {
      child.render(Bounds(bounds.x, bounds.y, preferredWidth(), preferredHeight()))
    }
  }
}