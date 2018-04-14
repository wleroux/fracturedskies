package com.fracturedskies.render.common.components.gl

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import org.lwjgl.opengl.GL11.*


class GLViewport(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  data class Padding(val top: Int = 0, val right: Int = 0, val bottom: Int = 0, val left: Int = 0)
  companion object {
    fun Node.Builder<*>.viewport(padding: Padding = Padding(), additionalProps: MultiTypeMap = MultiTypeMap(), block: Node.Builder<Unit>.() -> (Unit) = {}) {
      nodes.add(Node(::GLViewport, MultiTypeMap(
          PADDING to padding
      ).with(additionalProps), block))
    }
    val PADDING = TypedKey<Padding>("padding")
  }
  private val padding get() = props[PADDING]

  override fun glPreferredWidth(parentWidth: Int, parentHeight: Int): Int {
    return padding.left + padding.right + super.glPreferredWidth(parentWidth, parentHeight)
  }

  override fun glPreferredHeight(parentWidth: Int, parentHeight: Int): Int {
    return padding.top + padding.bottom + super.glPreferredHeight(parentWidth, parentHeight)
  }

  override fun glRender(bounds: Bounds) {
    val paddedBounds = Bounds(
        bounds.x + padding.left,
        bounds.y + padding.top,
        bounds.width - padding.left - padding.right,
        bounds.height - padding.top - padding.bottom
    )
    glViewport(paddedBounds.x, paddedBounds.y, paddedBounds.width, paddedBounds.height)
    glClear(GL_DEPTH_BUFFER_BIT)

    super.glRender(paddedBounds)
  }
}