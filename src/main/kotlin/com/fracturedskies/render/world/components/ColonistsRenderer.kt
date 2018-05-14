package com.fracturedskies.render.world.components

import com.fracturedskies.api.World
import com.fracturedskies.api.block.data.*
import com.fracturedskies.api.entity.Colonist
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.MeshParser
import com.fracturedskies.render.common.components.gl.glUniform
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glDrawElements
import org.lwjgl.opengl.GL30.glBindVertexArray
import javax.inject.Inject


class ColonistsRenderer : Component<Unit>(Unit) {
  companion object {
    fun Node.Builder<*>.colonists(sliceHeight: Int, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(ColonistsRenderer::class, MultiTypeMap(
          SLICE_HEIGHT to sliceHeight
      ).with(additionalProps)))
    }

    val SLICE_HEIGHT = TypedKey<Int>("sliceHeight")
  }

  @Inject
  private lateinit var world: World

  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean = false

  override fun glRender(bounds: Bounds) {
    super.glRender(bounds)

    val sliceHeight = props[SLICE_HEIGHT]
    world.colonists
        .filterValues { it.position.y <= sliceHeight }
        .forEach { _, colonist ->
          val mesh = colonistMesh(colonist)
          val rotation = Quaternion4.fromToRotation(Vector3.AXIS_NEG_Z, colonist.direction.toVector3())
          val modelPosition = colonist.position.toVector3() + Vector3(0.5f, 0f, 0.5f)
          glBindVertexArray(mesh.vao)
          glUniform(ColorShaderProgram.MODEL_LOCATION, Matrix4(position = modelPosition, rotation = rotation))
          glDrawElements(GL11.GL_TRIANGLES, mesh.indexCount, GL11.GL_UNSIGNED_INT, 0)
        }
  }

  var cache = mutableMapOf<Pair<Int, Int>, Mesh>()
  private fun colonistMesh(colonist: Colonist): Mesh {
    val block = world.blocks[colonist.position]
    val blockLight = block[BlockLight::class]!!.value
    val skyLight = block[SkyLight::class]!!.value
    return cache.computeIfAbsent(skyLight to blockLight, { _ ->
      Mesh.generate(MeshParser.generateQuads("colonist.mesh", skyLight, blockLight, emptyMap(), Vector3(-0.5f, 0f, -0.5f), 1f/16f).toList())
    })
  }
}