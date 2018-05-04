package com.fracturedskies.render.common.components.button

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.Matrix4.Companion.orthogonal
import com.fracturedskies.render.common.components.gl.glUniform
import com.fracturedskies.render.common.shaders.*
import com.fracturedskies.render.common.shaders.Mesh.Attribute
import com.fracturedskies.render.common.shaders.standard.StandardShaderProgram
import com.fracturedskies.render.common.shaders.standard.StandardShaderProgram.Companion.ALBEDO_LOCATION
import com.fracturedskies.render.common.shaders.standard.StandardShaderProgram.Companion.MODEL_LOCATION
import com.fracturedskies.render.common.shaders.standard.StandardShaderProgram.Companion.PROJECTION_LOCATION
import com.fracturedskies.render.common.shaders.standard.StandardShaderProgram.Companion.VIEW_LOCATION
import com.fracturedskies.render.common.style.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import java.lang.Math.*

class BaseComponent : Component<Unit>(Unit) {
  companion object {
    private val COLOR = TypedKey<Color4>("color")
    private val PADDING = TypedKey<Padding>("padding")
    private val MARGIN = TypedKey<Margin>("margin")
    private val BORDER = TypedKey<Border>("border")
    fun Node.Builder<*>.base(color: Color4 = Color4.WHITE, padding: Padding = Padding(), margin: Margin = Margin(), border: Border = Border(), additionalContext: MultiTypeMap = MultiTypeMap(), block: Node.Builder<*>.()->Unit = {}) {
      nodes.add(Node(BaseComponent::class, MultiTypeMap(
          COLOR to color,
          PADDING to padding,
          MARGIN to margin,
          BORDER to border
      ).with(additionalContext), block))
    }
  }

  private lateinit var program: StandardShaderProgram

  override fun componentDidMount() {
    super.componentDidMount()
    program = StandardShaderProgram()
  }

  private var texture: Texture? = null
  private var mesh: Mesh? = null

  override fun glPreferredWidth(parentWidth: Int, parentHeight: Int): Int {
    val horizontalMargin = props[MARGIN].left + props[MARGIN].right
    val horizontalPadding = props[PADDING].left + props[PADDING].right
    val horizontalBorder = props[BORDER].width.left + props[BORDER].width.right
    return horizontalMargin + horizontalPadding + horizontalBorder + super.glPreferredWidth(parentWidth, parentHeight)
  }

  override fun glPreferredHeight(parentWidth: Int, parentHeight: Int): Int {
    val verticalMargin = props[MARGIN].top + props[MARGIN].bottom
    val verticalPadding = props[PADDING].top + props[PADDING].bottom
    val verticalBorder = props[BORDER].width.top + props[BORDER].width.bottom
    return verticalMargin + verticalPadding + verticalBorder + super.glPreferredHeight(parentWidth, parentHeight)
  }

  override fun componentDidUpdate(prevProps: MultiTypeMap, prevState: Unit) {
    super.componentDidUpdate(prevProps, prevState)
    refreshButton()
  }

  private fun refreshButton() {
    val marginColor = Color4(0, 0, 0, 0).toFloat()
    val buttonColor = props[COLOR].toFloat()
    val borderColor = props[BORDER].color.toFloat()
    val pixels = BufferUtils.createByteBuffer(bounds.width * bounds.height * 4)
    val pixelColors = pixels.asFloatBuffer()
    (0 until bounds.height).forEach { y ->
      (0 until bounds.width).forEach { x ->
        pixelColors.put(when {
          isMargin(bounds, x, y) -> marginColor
          isBorder(bounds, x, y) -> borderColor
          else -> buttonColor
        })
      }
    }

    val mainColor = Color4.WHITE.toFloat()
    texture?.close()
    texture = Texture(bounds.width, bounds.height, pixels, GL_RGBA8, GL_RGBA)
    mesh?.close()
    mesh = Mesh(floatArrayOf(
        0f, 1f, 0f, 0f, 1f, 0f, mainColor,
        1f, 1f, 0f, 1f, 1f, 0f, mainColor,
        1f, 0f, 0f, 1f, 0f, 0f, mainColor,
        0f, 0f, 0f, 0f, 0f, 0f, mainColor
    ), intArrayOf(
        2, 1, 0,  0, 3, 2
    ), listOf(Attribute.POSITION, Attribute.TEXCOORD, Attribute.COLOR))
  }

  override fun glRender(bounds: Bounds) {
    if (this.bounds != bounds) {
      this.bounds = bounds
      refreshButton()
    }

    val projection = orthogonal(1f, 1f, 0f, 0f, -1f, 1f)
    program.bind {
      glViewport(bounds.x, bounds.y, bounds.width, bounds.height)
      glUniform(PROJECTION_LOCATION, projection)
      glUniform(VIEW_LOCATION, Matrix4.IDENTITY)
      glUniform(MODEL_LOCATION, Matrix4.IDENTITY)
      glUniform(ALBEDO_LOCATION, texture!!)

      glBindVertexArray(mesh!!.vao)
      glDrawElements(GL11.GL_TRIANGLES, mesh!!.indexCount, GL11.GL_UNSIGNED_INT, 0)
    }

    val (bTop, bRight, bBottom, bLeft) = props[BORDER].width
    val (pTop, pRight, pBottom, pLeft) = props[PADDING]
    val (mTop, mRight, mBottom, mLeft) = props[MARGIN]
    super.glRender(Bounds(
        bounds.x + mLeft + pLeft + bLeft,
        bounds.y + mBottom + pBottom + bBottom,
        bounds.width - mLeft - mRight - pLeft - pRight - bLeft - bRight,
        bounds.height - mTop - mBottom - pTop - pBottom - bTop - bBottom
    ))
  }

  private fun isMargin(bounds: Bounds, x: Int, y: Int): Boolean {
    val margin = props[MARGIN]
    val border = props[BORDER]

    if (bounds.height - margin.top <= y || y < margin.bottom) {
      return true
    }
    if (bounds.width - margin.right <= x || x < margin.left) {
      return true
    }

    val topLeftX = margin.left + border.radius.topLeft
    val topLeftY = bounds.height - margin.top - border.radius.topLeft
    if (x <= topLeftX && y > topLeftY) {
      val d = sqrt(pow((x - topLeftX).toDouble(), 2.0) + pow((y - topLeftY).toDouble(), 2.0))
      return (d > border.radius.topLeft)
    }

    val topRightX = bounds.width - margin.right - border.radius.topRight
    val topRightY = bounds.height - margin.top - border.radius.topRight
    if (x >= topRightX && y > topRightY) {
      val d = sqrt(pow((x - topRightX).toDouble(), 2.0) + pow((y - topRightY).toDouble(), 2.0))
      return (d > border.radius.topRight)
    }

    val bottomRightX = bounds.width - margin.right - border.radius.bottomRight
    val bottomRightY = margin.bottom + border.radius.bottomRight
    if (x >= bottomRightX && y < bottomRightY) {
      val d = sqrt(pow((x - bottomRightX).toDouble(), 2.0) + pow((y - bottomRightY).toDouble(), 2.0))
      return (d > border.radius.bottomRight)
    }

    val bottomLeftX = margin.left + border.radius.bottomLeft
    val bottomLeftY = margin.bottom + border.radius.bottomLeft
    if (x <= bottomLeftX && y <= bottomLeftY) {
      val d = sqrt(pow((x - bottomLeftX).toDouble(), 2.0) + pow((y - bottomLeftY).toDouble(), 2.0))
      return (d > border.radius.bottomLeft)
    }

    return false
  }


  private fun isBorder(bounds: Bounds, x: Int, y: Int): Boolean {
    val margin = props[MARGIN]
    val border = props[BORDER]

    if (bounds.height - margin.top - border.width.top <= y || y < margin.bottom + border.width.bottom) {
      return true
    }
    if (bounds.width - margin.right - border.width.right <= x || x < margin.left + border.width.left) {
      return true
    }

    val topLeftX = margin.left + border.radius.topLeft + border.width.left
    val topLeftY = bounds.height - margin.top - border.radius.topLeft - border.width.top
    if (x < topLeftX && y > topLeftY) {
      val d = sqrt(pow((x - topLeftX).toDouble(), 2.0) + pow((y - topLeftY).toDouble(), 2.0))
      return (d > border.radius.topLeft)
    }

    val topRightX = bounds.width - margin.right - border.radius.topRight - border.width.right
    val topRightY = bounds.height - margin.top - border.radius.topRight - border.width.top
    if (x > topRightX && y > topRightY) {
      val d = sqrt(pow((x - topRightX).toDouble(), 2.0) + pow((y - topRightY).toDouble(), 2.0))
      return (d > border.radius.topRight)
    }

    val bottomRightX = bounds.width - margin.right - border.radius.bottomRight - border.width.right
    val bottomRightY = margin.bottom + border.radius.bottomRight + border.width.bottom
    if (x > bottomRightX && y < bottomRightY) {
      val d = sqrt(pow((x - bottomRightX).toDouble(), 2.0) + pow((y - bottomRightY).toDouble(), 2.0))
      return (d > border.radius.bottomRight)
    }

    val bottomLeftX = margin.left + border.radius.bottomLeft + border.width.left
    val bottomLeftY = margin.bottom + border.radius.bottomLeft + border.width.bottom
    if (x < bottomLeftX && y < bottomLeftY) {
      val d = sqrt(pow((x - bottomLeftX).toDouble(), 2.0) + pow((y - bottomLeftY).toDouble(), 2.0))
      return (d > border.radius.bottomLeft)
    }

    return false
  }
}
