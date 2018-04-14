package com.fracturedskies.render.world.components

import com.fracturedskies.Item
import com.fracturedskies.api.BlockType
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


class ItemsRenderer(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.itemsRenderer(worldState: RenderWorldState, sliceHeight: Int, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::ItemsRenderer, MultiTypeMap(
          WORLD_STATE to worldState,
          SLICE_HEIGHT to sliceHeight
      ).with(additionalProps)))
    }

    private val WORLD_STATE = TypedKey<RenderWorldState>("worldState")
    private val SLICE_HEIGHT = TypedKey<Int>("sliceHeight")
  }

  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean = false

  override fun glRender(bounds: Bounds) {
    super.glRender(bounds)
    val sliceHeight = props[SLICE_HEIGHT]
    props[WORLD_STATE].items
        .filterValues { it.position.y <= sliceHeight }
        .map { (_, item) -> itemMesh(item) to item.position}
        .groupBy( {it.component1()}, {it.component2()} )
        .forEach { mesh, positions ->
          glBindVertexArray(mesh.vao)
          positions.distinct().forEach { position ->
            glUniform(ColorShaderProgram.MODEL_LOCATION, Matrix4(position = position.toVector3()))
            glDrawElements(GL11.GL_TRIANGLES, mesh.indexCount, GL11.GL_UNSIGNED_INT, 0)
          }
        }
  }

  var cache = mutableMapOf<BlockType, MutableMap<Pair<Int, Int>, Mesh>>()
  private fun itemMesh(item: Item): Mesh {
    val blockType = item.blockType
    val block = props[WORLD_STATE].blocks[item.position]
    val skyLight = block.skyLight
    val blockLight = block.blockLight
    return cache
        .computeIfAbsent(blockType, { mutableMapOf() })
        .computeIfAbsent(skyLight to blockLight, {
          generateBlock(blockType.color, skyLight.toFloat(), blockLight.toFloat(),
              Vector3(0.25f, 0.00f, 0.25f),
              Vector3(0.50f, 0.50f, 0.50f)
          ).invoke()
        })
  }
}