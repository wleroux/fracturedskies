package com.fracturedskies.render.common.shaders.noop

import com.fracturedskies.render.common.shaders.ShaderProgram

class NoopProgram : ShaderProgram(this::class.java.getResource("noop.vert").readText(), this::class.java.getResource("noop.frag").readText()) {
  companion object {
    private const val ALBEDO_LOCATION = 3
  }
}
