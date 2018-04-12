package com.fracturedskies.render.components.gl

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.Node.Builder
import com.fracturedskies.render.shaders.ShaderProgram


class GLShader(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.shader(shader: ShaderProgram, additionalProps: MultiTypeMap = MultiTypeMap(), block: Builder<Unit>.() -> Unit) {
      nodes.add(Node(::GLShader, MultiTypeMap(SHADER to shader).with(additionalProps), block))
    }

    val SHADER = TypedKey<ShaderProgram>("shader")
  }

  override fun glRender(bounds: Bounds) {
    props[SHADER].bind {
      super.glRender(bounds)
    }
  }
}