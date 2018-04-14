package com.fracturedskies.render.world.components

import com.fracturedskies.Colonist
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.GameState.RenderWorldState
import com.fracturedskies.render.common.components.gl.glUniform
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glDrawElements
import org.lwjgl.opengl.GL30.glBindVertexArray


class ColonistsRenderer(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.colonists(worldState: RenderWorldState, sliceHeight: Int, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::ColonistsRenderer, MultiTypeMap(
          WORLD_STATE to worldState,
          SLICE_HEIGHT to sliceHeight
      ).with(additionalProps)))
    }

    val WORLD_STATE = TypedKey<RenderWorldState>("worldState")
    val SLICE_HEIGHT = TypedKey<Int>("sliceHeight")
  }

  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean = false

  override fun glRender(bounds: Bounds) {
    super.glRender(bounds)

    val sliceHeight = props[SLICE_HEIGHT]
    props[WORLD_STATE].colonists
        .filterValues { it.position.y <= sliceHeight }
        .map { (_, colonist) -> colonistMesh(colonist) to colonist.position }
        .groupBy({it.component1()}, {it.component2()})
        .forEach { mesh, positions ->
          glBindVertexArray(mesh.vao)
          positions.distinct().forEach { position ->
            glUniform(ColorShaderProgram.MODEL_LOCATION, Matrix4(position = position.toVector3()))
            glDrawElements(GL11.GL_TRIANGLES, mesh.indexCount, GL11.GL_UNSIGNED_INT, 0)
          }
        }
  }

  var cache = mutableMapOf<Pair<Int, Int>, Mesh>()
  private fun colonistMesh(colonist: Colonist): Mesh {
    val block = props[WORLD_STATE].blocks[colonist.position]
    val blockLight = block.blockLight
    val skyLight = block.skyLight
    return cache.computeIfAbsent(skyLight to blockLight, { _ ->
      generateBlock(Color4.WHITE, skyLight.toFloat(), blockLight.toFloat(),
          Vector3(0.25f, 0.00f, 0.25f),
          Vector3(0.50f, 0.50f, 0.50f)
      ).invoke()
    })
  }
}