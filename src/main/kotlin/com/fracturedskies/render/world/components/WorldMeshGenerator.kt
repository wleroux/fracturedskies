package com.fracturedskies.render.world.components

import com.fracturedskies.api.block.Block
import com.fracturedskies.engine.collections.Space
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.common.shaders.Mesh

/**
 * A greedy mesh implementation
 */
fun generateWorldMesh(
    space: Space<Block>,
    sliceMesh: Boolean,
    xRange: IntRange, yRange: IntRange, zRange: IntRange
): Mesh {
  // Generate Mesh
  return Mesh.generate(xRange.asSequence().flatMap { x ->
    yRange.asSequence().flatMap { y ->
      zRange.asSequence().flatMap { z ->
        val pos = Vector3i(x, y, z)
        val block = space[pos]
        block.type.model.quads(space, pos, sliceMesh, Vector3.ZERO, 1f)
      }
    }
  }.toList())
}