package com.fracturedskies.api.block.model

import com.fracturedskies.api.block.Block
import com.fracturedskies.engine.collections.Space
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.world.Quad


interface BlockModel {
  fun quads(world: Space<Block>, pos: Vector3i, sliceMesh: Boolean, offset: Vector3, scale: Float): Sequence<Quad>
}