package com.fracturedskies.render.common.shaders.text

import com.fracturedskies.render.common.shaders.ShaderProgram

class TextShaderProgram: ShaderProgram(this::class.java.getResource("text.vert").readText(), this::class.java.getResource("text.frag").readText()) {
  companion object {
    const val PROJECTION_LOCATION = 2
    const val ALBEDO_LOCATION = 3
  }
}