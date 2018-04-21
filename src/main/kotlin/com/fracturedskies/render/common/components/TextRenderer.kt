package com.fracturedskies.render.common.components

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.event.*
import com.fracturedskies.engine.math.Color4
import com.fracturedskies.render.common.components.gl.GLMeshRenderer.Companion.mesh
import com.fracturedskies.render.common.components.gl.GLOrthogonal.Companion.orthogonal
import com.fracturedskies.render.common.components.gl.GLShader.Companion.shader
import com.fracturedskies.render.common.components.gl.GLUniform.Companion.uniform
import com.fracturedskies.render.common.components.gl.GLViewport.Companion.viewport
import com.fracturedskies.render.common.events.Click
import com.fracturedskies.render.common.shaders.*
import com.fracturedskies.render.common.shaders.Mesh.Attribute
import com.fracturedskies.render.common.shaders.text.TextShaderProgram
import com.fracturedskies.render.common.shaders.text.TextShaderProgram.Companion.ALBEDO_LOCATION
import com.fracturedskies.render.common.shaders.text.TextShaderProgram.Companion.PROJECTION_LOCATION
import org.lwjgl.glfw.GLFW
import org.lwjgl.stb.STBEasyFont.stb_easy_font_width


class TextRenderer(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    val TEXT = TypedKey<String>("text")
    val COLOR = TypedKey<Color4>("color")
    fun Node.Builder<*>.text(text: String, color: Color4 = Color4.WHITE) {
      nodes.add(Node(::TextRenderer, MultiTypeMap(
              TEXT to text,
              COLOR to color
      )))
    }
  }

  private val text get() = props[TEXT]
  private val color get() = props[COLOR]

  override fun glPreferredWidth(parentWidth: Int, parentHeight: Int) = textWidth
  override fun glPreferredHeight(parentWidth: Int, parentHeight: Int) = textHeight

  lateinit var font: TrueTypeFont
  lateinit var program: ShaderProgram
  override fun componentWillMount() {
    super.componentWillMount()
    program = TextShaderProgram()
    font = TrueTypeFont("OpenSans-Regular.ttf", 24)
  }

  override fun componentWillUnmount() {
    super.componentWillUnmount()
    program.close()
    mesh?.close()
  }

  var textWidth: Int = 0
  var textHeight: Int = 0
  var mesh: Mesh? = null
  override fun render() = nodes {
    val textVertices = font.getVertices(text)
    val vertices = FloatArray(textVertices.size * 7)
    textVertices.forEachIndexed { index, (positionX, positionY, texCoordX, texCoordY) ->
      vertices[index * 7 + 0] = positionX
      vertices[index * 7 + 1] = positionY
      vertices[index * 7 + 2] = 0f
      vertices[index * 7 + 3] = texCoordX
      vertices[index * 7 + 4] = texCoordY
      vertices[index * 7 + 5] = 0f
      vertices[index * 7 + 6] = color.toFloat()
    }
    val numQuads = (textVertices.size / 4)
    val indices = IntArray(numQuads * 6)
    (0 until numQuads).forEach { index ->
      indices[index * 6 + 0] = index * 4 + 2
      indices[index * 6 + 1] = index * 4 + 1
      indices[index * 6 + 2] = index * 4 + 0
      indices[index * 6 + 3] = index * 4 + 0
      indices[index * 6 + 4] = index * 4 + 3
      indices[index * 6 + 5] = index * 4 + 2
    }
    mesh?.close()
    textWidth = font.getWidth(text)
    textHeight = font.getHeight(text)
    mesh = Mesh(
        vertices,
        indices,
        listOf(Attribute.POSITION, Attribute.TEXCOORD, Attribute.COLOR)
    )

    viewport {
      shader(program) {
        orthogonal(PROJECTION_LOCATION, -1f, 1f)
        uniform(ALBEDO_LOCATION, font.texture)
        mesh(mesh!!)
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