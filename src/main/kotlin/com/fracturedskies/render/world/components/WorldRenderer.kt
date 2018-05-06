package com.fracturedskies.render.world.components

import com.fracturedskies.api.World
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.DirtyFlags
import com.fracturedskies.render.common.components.gl.GLPerspective.Companion.perspective
import com.fracturedskies.render.common.components.gl.GLShader.Companion.shader
import com.fracturedskies.render.common.components.gl.GLUniform.Companion.uniform
import com.fracturedskies.render.common.components.gl.GLViewport.Companion.viewport
import com.fracturedskies.render.common.shaders.ShaderProgram
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram.Companion.PROJECTION_LOCATION
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram.Companion.VIEW_LOCATION
import com.fracturedskies.render.world.components.BlocksRenderer.Companion.blocks
import com.fracturedskies.render.world.components.ColonistsRenderer.Companion.colonists
import com.fracturedskies.render.world.components.ItemsRenderer.Companion.items
import com.fracturedskies.render.world.components.LightUniform.Companion.lightUniform
import com.fracturedskies.render.world.components.SelectionRenderer.Companion.selection
import com.fracturedskies.render.world.components.WaterRenderer.Companion.water
import com.fracturedskies.render.world.components.ZonesRenderer.Companion.zones
import javax.inject.Inject


class WorldRenderer : Component<Unit>(Unit) {
  companion object {
    fun Node.Builder<*>.world(view: Matrix4, selection: Pair<Vector3i, Vector3i>?, areaColor: Color4, sliceHeight: Int, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(WorldRenderer::class, MultiTypeMap(
          VIEW to view,
          SELECTION to selection,
          AREA_COLOR to areaColor,
          SLICE_HEIGHT to sliceHeight
      ).with(additionalProps)))
    }

    val AREA_COLOR = TypedKey<Color4>("areaColor")
    val SELECTION = TypedKey<Pair<Vector3i, Vector3i>?>("selection")
    val VIEW = TypedKey<Matrix4>("view")
    val SLICE_HEIGHT = TypedKey<Int>("sliceHeight")
  }

  @Inject
  private lateinit var world: World

  @Inject
  private lateinit var dirtyFlags: DirtyFlags

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
        lightUniform()

        val sliceHeight = props[SLICE_HEIGHT]
        blocks(sliceHeight)
        colonists(sliceHeight)
        items(sliceHeight)
        water(sliceHeight)

        zones(sliceHeight)

        selection(props[SELECTION], props[AREA_COLOR])
      }
    }
  }
}