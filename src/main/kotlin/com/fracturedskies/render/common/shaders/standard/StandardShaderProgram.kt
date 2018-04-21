package com.fracturedskies.render.common.shaders.standard

import com.fracturedskies.render.common.shaders.ShaderProgram

class StandardShaderProgram: ShaderProgram(this::class.java.getResource("standard.vert").readText(), this::class.java.getResource("standard.frag").readText()) {
  companion object {
    const val MODEL_LOCATION = 0
    const val VIEW_LOCATION = 1
    const val PROJECTION_LOCATION = 2
    const val ALBEDO_LOCATION = 3
  }
}
