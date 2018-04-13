package com.fracturedskies.render.common.components

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.event.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.common.components.gl.GLMeshRenderer.Companion.meshRenderer
import com.fracturedskies.render.common.components.gl.GLShader.Companion.shader
import com.fracturedskies.render.common.components.gl.GLUniform.Companion.uniform
import com.fracturedskies.render.common.components.gl.GLViewport.Companion.viewport
import com.fracturedskies.render.common.events.Click
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.common.shaders.Mesh.Attribute
import com.fracturedskies.render.common.shaders.text.TextShaderProgram
import com.fracturedskies.render.common.shaders.text.TextShaderProgram.Companion.PROJECTION_LOCATION
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import org.lwjgl.stb.STBEasyFont
import org.lwjgl.stb.STBEasyFont.*

class TextRenderer(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    val TEXT = TypedKey<String>("text")
    val COLOR = TypedKey<Color4>("color")
    fun Node.Builder<*>.textRenderer(text: String, color: Color4 = Color4.WHITE) {
      nodes.add(Node(::TextRenderer, MultiTypeMap(
              TEXT to text,
              COLOR to color
      )))
    }
  }

  private val text get() = props[TEXT]
  private val color get() = props[COLOR]

  override fun glPreferredWidth(parentWidth: Int, parentHeight: Int) = stb_easy_font_width(text)
  override fun glPreferredHeight(parentWidth: Int, parentHeight: Int) = stb_easy_font_height(text)

  lateinit var program: TextShaderProgram
  override fun componentWillMount() {
    super.componentWillMount()
    program = TextShaderProgram()
  }

  override fun componentWillUnmount() {
    super.componentWillUnmount()
    program.close()
  }

  private val colorBuffer = BufferUtils.createByteBuffer(4)
  private val buffer = BufferUtils.createByteBuffer(999990)
  override fun render() = nodes {
    colorBuffer.put(color.red.toByte())
    colorBuffer.put(color.green.toByte())
    colorBuffer.put(color.blue.toByte())
    colorBuffer.put(color.alpha.toByte())
    colorBuffer.flip()
    val numQuads = STBEasyFont.stb_easy_font_print(0f, 0f, text, colorBuffer, buffer)
    val floatBuffer = buffer.asFloatBuffer()
    val vertices = FloatArray(numQuads * 4 * 4)
    floatBuffer.get(vertices)
    val indices = IntArray(numQuads * 6)
    for (quad in 0 until numQuads) {
      indices[(quad * 6) + 0] = (quad * 4) + 0
      indices[(quad * 6) + 1] = (quad * 4) + 1
      indices[(quad * 6) + 2] = (quad * 4) + 2
      indices[(quad * 6) + 3] = (quad * 4) + 2
      indices[(quad * 6) + 4] = (quad * 4) + 3
      indices[(quad * 6) + 5] = (quad * 4) + 0
    }
    val textWidth = stb_easy_font_width(text)
    val textHeight = stb_easy_font_height(text)
    val projection = Matrix4.orthogonal(0f, textWidth.toFloat(), textHeight.toFloat(), 0f, -1f, 1000f)

    viewport {
      shader(program) {
        uniform(PROJECTION_LOCATION, projection)
        meshRenderer(Mesh(
            vertices,
            indices,
            listOf(Attribute.POSITION, Attribute.COLOR)
        ))
      }
    }
  }

  private fun doOnClick(event: Click) {
    if (event.action != GLFW.GLFW_PRESS)
      return

    val offset = event.mousePos.x - this.bounds.x
    val cursorPosition = if (offset > stb_easy_font_width(text)) {
      text.length
    } else {
      var prevWidth = 0
      var temp = 0
      for (i in 0..text.length) {
        val subTextWidth = stb_easy_font_width(text.substring(0, i))
        val halfPoint = prevWidth + (subTextWidth - prevWidth / 2)
        if (offset < prevWidth) {
          break
        } else if (offset < halfPoint) {
          temp = i
        } else if (offset > halfPoint){
          temp = i + 1
        }
        prevWidth = subTextWidth
      }
      temp
    }

    dispatch(this, CursorPosition(this, cursorPosition))
  }

  class CursorPosition(target: Component<*>, val cursorPosition: Int) : Event(target)

  override val handler = EventHandlers(
          on(Click::class) { doOnClick(it) }
  )
}