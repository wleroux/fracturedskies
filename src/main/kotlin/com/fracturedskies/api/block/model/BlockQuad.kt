package com.fracturedskies.api.block.model

import com.fracturedskies.engine.math.Color4


data class BlockQuad(
    val dx: Float, val dy: Float, val dz: Float,
    val width: Float, val height: Float,
    val color: Color4
)