package com.fracturedskies.render.world.components

import com.fracturedskies.Block
import com.fracturedskies.api.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.light.api.MAX_LIGHT_LEVEL
import com.fracturedskies.render.GameState.RenderWorldState
import com.fracturedskies.render.common.components.gl.glUniform
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import java.lang.Integer.*


class ZonesRenderer(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.zones(world: RenderWorldState, sliceHeight: Int, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::ZonesRenderer, MultiTypeMap(
          WORLD to world,
          SLICE_HEIGHT to sliceHeight
      ).with(additionalProps)))
    }

    private val WORLD = TypedKey<RenderWorldState>("world")
    private val SLICE_HEIGHT = TypedKey<Int>("sliceHeight")
  }

  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean = false

  var cache = mutableMapOf<Id, Mesh>()
  override fun glRender(bounds: Bounds) {
    super.glRender(bounds)
    val world = props[WORLD]
    world.zones.forEach { _, zone ->
      val mesh = cache.computeIfAbsent(zone.id, { _ ->
        var minX: Int = Int.MAX_VALUE
        var minY: Int = Int.MAX_VALUE
        var minZ: Int = Int.MAX_VALUE
        var maxX: Int = Int.MIN_VALUE
        var maxY: Int = Int.MIN_VALUE
        var maxZ: Int = Int.MIN_VALUE
        zone.positions.forEach { pos ->
          minX = min(minX, pos.x)
          minY = min(minY, pos.y)
          minZ = min(minZ, pos.z)
          maxX = max(maxX, pos.x)
          maxY = max(maxY, pos.y)
          maxZ = max(maxZ, pos.z)
        }


        generateWorldMesh(object : Space<Block> {
          override val dimension: Dimension = world.dimension
          override fun get(index: Int): Block {
            val pos = dimension.toVector3i(index)
            return if (zone.positions.contains(pos)) {
              Block(ZoneBlockType, MAX_LIGHT_LEVEL, MAX_LIGHT_LEVEL, 0.toByte())
            } else {
              Block(BlockAir, MAX_LIGHT_LEVEL, MAX_LIGHT_LEVEL, 0.toByte())
            }
          }
        }, false, (minX - 1 .. maxX + 1), (minY - 1 .. maxY + 1), (minZ - 1 .. maxZ + 2)).invoke()
      })

      glDisable(GL_DEPTH_TEST)
      glBindVertexArray(mesh.vao)
      glUniform(ColorShaderProgram.MODEL_LOCATION, Matrix4())
      glDrawElements(GL11.GL_TRIANGLES, mesh.indexCount, GL11.GL_UNSIGNED_INT, 0)
      glEnable(GL_DEPTH_TEST)
    }
  }
}

private object ZoneBlockType: BlockType() {
  override val color: Color4 = Color4(255, 255, 255, 32)
}