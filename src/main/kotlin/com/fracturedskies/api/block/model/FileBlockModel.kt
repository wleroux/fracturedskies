package com.fracturedskies.api.block.model

import com.fracturedskies.api.block.Block
import com.fracturedskies.api.block.data.*
import com.fracturedskies.engine.collections.Space
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.MeshParser
import com.fracturedskies.render.world.Quad


class FileBlockModel(private val file: String): BlockModel {
  override fun quads(world: Space<Block>, pos: Vector3i, sliceMesh: Boolean, offset: Vector3, scale: Float): Sequence<Quad> {
    val block = world[pos]
    val skyLight = block[SkyLight::class]!!.value
    val blockLight = block[BlockLight::class]!!.value
    return MeshParser.generateQuads(file, skyLight, blockLight, emptyMap(), pos.toVector3() + offset * scale, 1f/16f * scale)
  }
}