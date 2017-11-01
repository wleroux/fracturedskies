package com.fracturedskies.render.components

import com.fracturedskies.engine.jeact.Node

interface Renderable {
  fun render()
}

fun Node.render() {
  if (this.component is Renderable) {
    this.component.render()
  }
  this.children.forEach({it.render()})
}
