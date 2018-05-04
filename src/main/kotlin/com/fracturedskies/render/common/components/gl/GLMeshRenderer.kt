package com.fracturedskies.render.common.components.gl

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.render.common.shaders.Mesh
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glDrawElements
import org.lwjgl.opengl.GL30.glBindVertexArray


class GLMeshRenderer : Component<Unit>(Unit) {
  companion object {
    fun Node.Builder<*>.mesh(mesh: Mesh, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(GLMeshRenderer::class, MultiTypeMap(
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