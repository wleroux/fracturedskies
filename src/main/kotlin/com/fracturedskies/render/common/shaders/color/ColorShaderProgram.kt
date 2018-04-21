package com.fracturedskies.render.common.shaders.color

import com.fracturedskies.render.common.shaders.ShaderProgram

class ColorShaderProgram : ShaderProgram(this::class.java.getResource("color.vert").readText(), this::class.java.getResource("color.frag").readText()) {
  companion object {
    const val MODEL_LOCATION = 0
    const val VIEW_LOCATION = 1
    const val PROJECTION_LOCATION = 2

    const val LIGHT_DIRECTION_LOCATION = 3
    const val SKY_COLORS_LOCATION = 4
    const val BLOCK_COLORS_LOCATION = 20
  }
}
