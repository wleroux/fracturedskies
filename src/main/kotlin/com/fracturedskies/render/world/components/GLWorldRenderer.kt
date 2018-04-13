package com.fracturedskies.render.world.components

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.common.components.gl.GLPerspective.Companion.perspective
import com.fracturedskies.render.common.components.gl.GLShader.Companion.shader
import com.fracturedskies.render.common.components.gl.GLUniform.Companion.uniform
import com.fracturedskies.render.common.components.gl.GLViewport.Companion.viewport
import com.fracturedskies.render.common.shaders.ShaderProgram
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram.Companion.BLOCK_COLORS_LOCATION
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram.Companion.LIGHT_DIRECTION_LOCATION
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram.Companion.MODEL_LOCATION
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram.Companion.PROJECTION_LOCATION
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram.Companion.SKY_COLORS_LOCATION
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram.Companion.VIEW_LOCATION
import com.fracturedskies.render.world.*
import com.fracturedskies.render.world.components.GLBlockRenderer.Companion.blockRenderer
import com.fracturedskies.render.world.components.GLColonistRenderer.Companion.colonistRenderer
import com.fracturedskies.render.world.components.GLItemRenderer.Companion.itemRenderer
import com.fracturedskies.render.world.components.GLWaterRenderer.Companion.waterRenderer
import com.fracturedskies.task.Item
import java.nio.IntBuffer
import kotlin.math.PI


class GLWorldRenderer(props: MultiTypeMap): Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.worldRenderer(view: Matrix4, timeOfDay: Float, blocks: ChunkSpace<Block>, sliceHeight: Int, colonists: Collection<Worker>, items: Collection<Item>, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::GLWorldRenderer, MultiTypeMap(
          VIEW to view,
          TIME_OF_DAY to timeOfDay,
          BLOCKS to blocks,
          SLICE_HEIGHT to sliceHeight,
          COLONISTS to colonists,
          ITEMS to items
      ).with(additionalProps)))
    }

    val VIEW = TypedKey<Matrix4>("view")

    val TIME_OF_DAY = TypedKey<Float>("timeOfDay")

    val BLOCKS = TypedKey<ChunkSpace<Block>>("blocks")
    val SLICE_HEIGHT = TypedKey<Int>("sliceHeight")

    val COLONISTS = TypedKey<Collection<Worker>>("colonists")
    val ITEMS = TypedKey<Collection<Item>>("items")
  }

  private lateinit var shader: ShaderProgram
  private lateinit var skyLightLevels: LightLevels
  private lateinit var blockLightLevels: LightLevels
  override fun componentWillMount() {
    super.componentWillMount()
    shader = ColorShaderProgram()
    skyLightLevels = LightLevels.load(loadByteBuffer("SkyLightLevels.png", this::class.java), 240)
    blockLightLevels = LightLevels.load(loadByteBuffer("BlockLightLevels.png", this::class.java), 16)
  }

  private val skyLightDirection = Vector3(0f, -1f, -0.5f).normalize()
  private lateinit var lightDirection: Vector3
  private lateinit var skyLight: IntBuffer
  private lateinit var blockLight: IntBuffer
  override fun componentWillUpdate(nextProps: MultiTypeMap, nextState: Unit) {
    super.componentWillUpdate(nextProps, nextState)

    lightDirection = skyLightDirection * Quaternion4(Vector3.AXIS_Z, nextProps[TIME_OF_DAY] * 2f * PI.toFloat())
    skyLight = skyLightLevels.getColorBuffer(nextProps[TIME_OF_DAY])
    blockLight = blockLightLevels.getColorBuffer(nextProps[TIME_OF_DAY])
  }

  override fun render() = nodes {
    viewport {
      shader(shader) {
        perspective(PROJECTION_LOCATION, Math.PI.toFloat() / 4f, 0.03f, 1000f)
        uniform(VIEW_LOCATION, props[VIEW])
        uniform(MODEL_LOCATION, Matrix4.IDENTITY)

        uniform(LIGHT_DIRECTION_LOCATION, lightDirection)
        uniform(SKY_COLORS_LOCATION, skyLight)
        uniform(BLOCK_COLORS_LOCATION, blockLight)

        val sliceHeight = props[SLICE_HEIGHT]

        // Blocks
        uniform(MODEL_LOCATION, Matrix4.IDENTITY)
        props[BLOCKS].chunks.forEach { position, _ ->
          blockRenderer(props[BLOCKS], position, sliceHeight)
        }

        // Colonists
        props[COLONISTS].forEach { colonist ->
          if (colonist.pos.y < sliceHeight) {
            uniform(MODEL_LOCATION, Matrix4(position = colonist.pos.toVector3()))
            val block = props[BLOCKS][colonist.pos]
            colonistRenderer(colonist, block.skyLight, block.blockLight)
          }
        }

        // Items
        props[ITEMS].forEach { item ->
          if (item.position.y < sliceHeight) {
            val block = props[BLOCKS][item.position]
            uniform(MODEL_LOCATION, Matrix4(position = item.position.toVector3()))
            itemRenderer(item, block.skyLight, block.blockLight)
          }
        }

        // Water
        uniform(MODEL_LOCATION, Matrix4.IDENTITY)
        props[BLOCKS].chunks.forEach { position, _ ->
          waterRenderer(props[BLOCKS], position, sliceHeight)
        }
      }
    }
  }
}