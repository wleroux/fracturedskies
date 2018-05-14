package com.fracturedskies.render.world.components

import com.fracturedskies.api.World
import com.fracturedskies.api.block.data.*
import com.fracturedskies.api.entity.ItemType
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.common.components.gl.glUniform
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glDrawElements
import org.lwjgl.opengl.GL30.glBindVertexArray
import javax.inject.Inject


class ItemsRenderer : Component<Unit>(Unit) {
  companion object {
    fun Node.Builder<*>.items(sliceHeight: Int, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(ItemsRenderer::class, MultiTypeMap(
          SLICE_HEIGHT to sliceHeight
      ).with(additionalProps)))
    }

    private val SLICE_HEIGHT = TypedKey<Int>("sliceHeight")
  }

  @Inject
  private lateinit var world: World

  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean = false

  override fun glRender(bounds: Bounds) {
    super.glRender(bounds)
    val sliceHeight = props[SLICE_HEIGHT]
    world.items
        .filterValues { it.position != null }
        .filterValues { it.position!!.y <= sliceHeight }
        .map { (_, item) ->
          val block = world.blocks[item.position!!]
          itemMesh(item.itemType, block[SkyLight::class]!!.value, block[BlockLight::class]!!.value) to item.position!!}
        .groupBy( {it.component1()}, {it.component2()} )
        .forEach { mesh, positions ->
          glBindVertexArray(mesh.vao)
          positions.distinct().forEach { position ->
            glUniform(ColorShaderProgram.MODEL_LOCATION, Matrix4(position = position.toVector3()))
            glDrawElements(GL11.GL_TRIANGLES, mesh.indexCount, GL11.GL_UNSIGNED_INT, 0)
          }
        }
  }

  private var cache = mutableMapOf<ItemType, MutableMap<Pair<Int, Int>, Mesh>>()
  private fun itemMesh(itemType: ItemType, skyLight: Int, blockLight: Int): Mesh {
    return cache
        .computeIfAbsent(itemType, { mutableMapOf() })
        .computeIfAbsent(skyLight to blockLight, {
          Mesh.generate(itemType.model.quads(skyLight, blockLight, Vector3(0.375f, 0f, 0.375f), 1f/4f).toList())
        })
  }
}