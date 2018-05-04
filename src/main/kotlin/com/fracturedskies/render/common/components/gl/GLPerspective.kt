package com.fracturedskies.render.common.components.gl

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.engine.math.Matrix4.Companion.perspective
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20.glUniformMatrix4fv


class GLPerspective : Component<Unit>(Unit) {
  companion object {
    fun Node.Builder<*>.perspective(location: Int, fov: Float = Math.PI.toFloat() / 4, near: Float = 0.03f, far: Float = 1000f, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(GLPerspective::class, MultiTypeMap(
          LOCATION to location,
          FIELD_OF_VIEW to fov,
          NEAR to near,
          FAR to far
      ).with(additionalProps)))
    }

    val LOCATION = TypedKey<Int>("location")
    val FIELD_OF_VIEW = TypedKey<Float>("fov")
    val NEAR = TypedKey<Float>("near")
    val FAR = TypedKey<Float>("far")
  }

  override fun componentWillUpdate(nextProps: MultiTypeMap, nextState: Unit) {
    super.componentWillUpdate(nextProps, nextState)

    // Reset project
    this.bounds = Bounds(0, 0, 0, 0)
  }

  private val mat4Buffer = BufferUtils.createFloatBuffer(16)
  private lateinit var projection: Matrix4
  override fun glRender(bounds: Bounds) {
    if (bounds != this.bounds)
      projection = perspective(props[FIELD_OF_VIEW], bounds.width, bounds.height, props[NEAR], props[FAR])
    super.glRender(bounds)

    projection.store(mat4Buffer)
    mat4Buffer.flip()
    glUniformMatrix4fv(props[LOCATION], false, mat4Buffer)
  }
}