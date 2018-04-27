package com.fracturedskies.render.world.components

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.light.api.MAX_LIGHT_LEVEL
import com.fracturedskies.render.common.components.gl.glUniform
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.glBindVertexArray


class AreaRenderer(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.area(area: Pair<Vector3i, Vector3i>?, color: Color4 = Color4(255, 255, 255, 48), additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::AreaRenderer, MultiTypeMap(
          AREA to area,
          COLOR to color
      ).with(additionalProps)))
    }

    private val COLOR = TypedKey<Color4>("color")
    private val AREA = TypedKey<Pair<Vector3i,Vector3i>?>("area")
  }

  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean = false

  override fun glRender(bounds: Bounds) {
    super.glRender(bounds)
    val area = props[AREA]
    val color = props[COLOR]
    if (area != null) {
      val (start, end) = area
      val dimension = end - start
      val mesh = generateBlock(color, MAX_LIGHT_LEVEL.toFloat(), MAX_LIGHT_LEVEL.toFloat(), Vector3.ZERO, dimension.toVector3()).invoke()
      glDisable(GL_DEPTH_TEST)
      glBindVertexArray(mesh.vao)
      glUniform(ColorShaderProgram.MODEL_LOCATION, Matrix4(position = start.toVector3()))
      glDrawElements(GL11.GL_TRIANGLES, mesh.indexCount, GL11.GL_UNSIGNED_INT, 0)
      glEnable(GL_DEPTH_TEST)
    }
  }
}