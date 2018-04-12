package com.fracturedskies.render.components.gl

import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.*
import org.lwjgl.opengl.GL11.glViewport


class GLViewport(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.viewport(additionalProps: MultiTypeMap = MultiTypeMap(), block: Node.Builder<Unit>.() -> (Unit) = {}) {
      nodes.add(Node(::GLViewport, MultiTypeMap().with(additionalProps), block))
    }
  }

  override fun glRender(bounds: Bounds) {
    glViewport(bounds.x, bounds.y, bounds.width, bounds.height)
    super.glRender(bounds)
  }
}