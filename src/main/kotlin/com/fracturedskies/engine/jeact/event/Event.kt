package com.fracturedskies.engine.jeact.event

import com.fracturedskies.engine.jeact.Component

open class Event(val target: Component) {
  var phase: Phase = Phase.CAPTURE
  var stopPropogation: Boolean = false

  override fun toString() = "${this.javaClass.simpleName}[$phase]($target)"
}

enum class Phase {
  CAPTURE,
  TARGET,
  BUBBLE
}