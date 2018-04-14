package com.fracturedskies.render.world.components

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.GameState.RenderWorldState
import com.fracturedskies.render.common.components.gl.GLPerspective.Companion.perspective
import com.fracturedskies.render.common.components.gl.GLShader.Companion.shader
import com.fracturedskies.render.common.components.gl.GLUniform.Companion.uniform
import com.fracturedskies.render.common.components.gl.GLViewport.Companion.viewport
import com.fracturedskies.render.common.shaders.ShaderProgram
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram.Companion.PROJECTION_LOCATION
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram.Companion.VIEW_LOCATION
import com.fracturedskies.render.world.components.BlocksRenderer.Companion.blocksRenderer
import com.fracturedskies.render.world.components.ColonistsRenderer.Companion.colonistsRenderer
import com.fracturedskies.render.world.components.ItemsRenderer.Companion.itemsRenderer
import com.fracturedskies.render.world.components.LightUniform.Companion.lightUniform
import com.fracturedskies.render.world.components.WaterRenderer.Companion.waterRenderer


class WorldRenderer(props: MultiTypeMap): Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.worldRenderer(worldState: RenderWorldState, view: Matrix4, sliceHeight: Int, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::WorldRenderer, MultiTypeMap(
          WORLD_STATE to worldState,
          VIEW to view,
          SLICE_HEIGHT to sliceHeight
      ).with(additionalProps)))
    }

    val VIEW = TypedKey<Matrix4>("view")
    val WORLD_STATE = TypedKey<RenderWorldState>("worldState")
    val SLICE_HEIGHT = TypedKey<Int>("sliceHeight")
  }

  private lateinit var shader: ShaderProgram
  override fun componentWillMount() {
    super.componentWillMount()
    shader = ColorShaderProgram()
  }

  override fun render() = nodes {
    viewport {
      shader(shader) {
        perspective(PROJECTION_LOCATION, Math.PI.toFloat() / 4f, 0.03f, 1000f)
        uniform(VIEW_LOCATION, props[VIEW])
        lightUniform(props[WORLD_STATE])

        val worldState = props[WORLD_STATE]
        val sliceHeight = props[SLICE_HEIGHT]
        blocksRenderer(worldState, sliceHeight)
        colonistsRenderer(worldState, sliceHeight)
        itemsRenderer(worldState, sliceHeight)
        waterRenderer(worldState, sliceHeight)
      }
    }
  }
}