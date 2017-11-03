package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds

class Card(attributes: Context) : AbstractComponent<Unit>(attributes, Unit) {
  override fun render(bounds: Bounds) {
    this.bounds = bounds
    for (child in children) {
      child.render(bounds)
    }
  }
}