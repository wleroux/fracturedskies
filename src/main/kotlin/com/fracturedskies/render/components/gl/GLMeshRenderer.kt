package com.fracturedskies.render.components.gl

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.render.shaders.Mesh
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glDrawElements
import org.lwjgl.opengl.GL30.glBindVertexArray


class GLMeshRenderer(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.meshRenderer(mesh: Mesh, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::GLMeshRenderer, MultiTypeMap(
          MESH to mesh
      ).with(additionalProps)))
    }

    val MESH = TypedKey<Mesh>("mesh")
  }

  override fun glRender(bounds: Bounds) {
    super.glRender(bounds)

    val mesh = props[MESH]
    glBindVertexArray(mesh.vao)
    glDrawElements(GL11.GL_TRIANGLES, mesh.indexCount, GL11.GL_UNSIGNED_INT, 0)
  }
}