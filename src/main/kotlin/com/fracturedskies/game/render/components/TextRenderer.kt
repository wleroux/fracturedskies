package com.fracturedskies.game.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Component
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.event.Event
import com.fracturedskies.engine.jeact.event.EventHandlers
import com.fracturedskies.engine.jeact.event.on
import com.fracturedskies.engine.jeact.nodes
import com.fracturedskies.engine.math.Color4
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.game.render.components.MeshRenderer.Companion.meshRenderer
import com.fracturedskies.game.render.events.Click
import com.fracturedskies.game.render.shaders.Material
import com.fracturedskies.game.render.shaders.Mesh
import com.fracturedskies.game.render.shaders.Mesh.Attribute
import com.fracturedskies.game.render.shaders.text.TextShaderProgram
import com.fracturedskies.game.render.shaders.text.TextShaderProgram.Companion.PROJECTION
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import org.lwjgl.stb.STBEasyFont
import org.lwjgl.stb.STBEasyFont.stb_easy_font_height
import org.lwjgl.stb.STBEasyFont.stb_easy_font_width

class TextRenderer(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  companion object {
    val TEXT = Key<String>("text")
    val COLOR = Key<Color4>("color")
    fun Node.Builder<*>.textRenderer(text: String, color: Color4 = Color4.WHITE) {
      nodes.add(Node(::TextRenderer, Context(
              TEXT to text,
              COLOR to color
      )))
    }
  }

  private val text get() = requireNotNull(attributes[TEXT])
  private val color get() = requireNotNull(attributes[COLOR])

  override fun preferredWidth(parentWidth: Int, parentHeight: Int) = stb_easy_font_width(text)
  override fun preferredHeight(parentWidth: Int, parentHeight: Int) = stb_easy_font_height(text)
  var mesh: Mesh? = null

  override fun toNode() = nodes {
    val colorBuffer = BufferUtils.createByteBuffer(4)
    colorBuffer.put(color.red.toByte())
    colorBuffer.put(color.green.toByte())
    colorBuffer.put(color.blue.toByte())
    colorBuffer.put(color.alpha.toByte())
    colorBuffer.flip()
    val buffer = BufferUtils.createByteBuffer(999990)
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
    val material = Material(TextShaderProgram(), Context())
    mesh?.close()
    mesh = Mesh(
        vertices,
        indices,
        listOf(Attribute.POSITION, Attribute.COLOR)
    )
    val textWidth = stb_easy_font_width(text)
    val textHeight = stb_easy_font_height(text)
    val projection = Matrix4.orthogonal(0f, textWidth.toFloat(), textHeight.toFloat(), 0f, -1f, 1000f)

    meshRenderer(mesh!!, material, Context(
            PROJECTION to projection
    ))
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

    dispatch(CursorPosition(this, cursorPosition))
  }

  class CursorPosition(target: Component<*>, val cursorPosition: Int) : Event(target)

  override val handler = EventHandlers(
          on(Click::class) { doOnClick(it) }
  )
}