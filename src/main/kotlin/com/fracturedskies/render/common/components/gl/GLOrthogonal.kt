package com.fracturedskies.render.common.components.gl

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.engine.math.Matrix4.Companion.orthogonal


class GLOrthogonal(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.orthogonal(location: Int, near: Float = 0.03f, far: Float = 1000f, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::GLOrthogonal, MultiTypeMap(
          LOCATION to location,
          NEAR to near,
          FAR to far
      ).with(additionalProps)))
    }

    val LOCATION = TypedKey<Int>("location")
    val NEAR = TypedKey<Float>("near")
    val FAR = TypedKey<Float>("far")
  }

  override fun componentWillUpdate(nextProps: MultiTypeMap, nextState: Unit) {
    super.componentWillUpdate(nextProps, nextState)

    // Reset projection
    this.bounds = Bounds(0, 0, 0, 0)
  }

  private lateinit var projection: Matrix4
  override fun glRender(bounds: Bounds) {
    if (bounds != this.bounds)
      projection = orthogonal(bounds.height.toFloat(), bounds.width.toFloat(), 0f, 0f, props[NEAR], props[FAR])
    glUniform(props[LOCATION], projection)
    super.glRender(bounds)
  }
}