package com.fracturedskies.render.world.components

import com.fracturedskies.api.World
import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.common.components.gl.glUniform
import com.fracturedskies.render.common.shaders.color.ColorShaderProgram
import com.fracturedskies.render.world.LightLevels
import javax.inject.Inject
import kotlin.math.PI


class LightUniform : Component<Unit>(Unit) {
  companion object {
    fun Node.Builder<*>.lightUniform(additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(LightUniform::class, additionalProps))
    }

    private val skyLightDirection = Vector3(0f, -1f, -0.5f).normalize()
  }

  @Inject
  private lateinit var world: World

  private lateinit var skyLightLevels: LightLevels
  private lateinit var blockLightLevels: LightLevels
  override fun componentDidMount() {
    skyLightLevels = LightLevels.load(loadByteBuffer("SkyLightLevels.png", this::class.java), 240)
    blockLightLevels = LightLevels.load(loadByteBuffer("BlockLightLevels.png", this::class.java), 16)
  }

  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean = false

  override fun glRender(bounds: Bounds) {
    super.glRender(bounds)

    val timeOfDay = world.timeOfDay
    val lightDirection = skyLightDirection * Quaternion4(Vector3.AXIS_Z, timeOfDay * 2f * PI.toFloat())
    val skyLight = skyLightLevels.getColorBuffer(timeOfDay)
    val blockLight = blockLightLevels.getColorBuffer(timeOfDay)

    glUniform(ColorShaderProgram.LIGHT_DIRECTION_LOCATION, lightDirection)
    glUniform(ColorShaderProgram.SKY_COLORS_LOCATION, skyLight)
    glUniform(ColorShaderProgram.BLOCK_COLORS_LOCATION, blockLight)
  }
}