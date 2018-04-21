package com.fracturedskies.render.common.style

import com.fracturedskies.engine.math.Color4

data class Border(val width: BorderWidth = BorderWidth(), val radius: BorderRadius = BorderRadius(), val color: Color4 = Color4.DARK_BROWN) {
  data class BorderWidth(val top: Int = 0, val right: Int = 0, val bottom: Int = 0, val left: Int = 0)
  data class BorderRadius(val topLeft: Int = 0, val topRight: Int = 0, val bottomRight: Int = 0, val bottomLeft: Int = 0)
}
