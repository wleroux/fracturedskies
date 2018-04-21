package com.fracturedskies.worldgenerator

import com.fracturedskies.Block
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.collections.*


class WorldGenerator(
    private val biomeGenerator: BiomeGenerator,
    private val generationPopulator: GenerationPopulator,
    private val populators: List<Populator>
) {
  fun generate(dimension: Dimension): ObjectMutableSpace<Block> {
    val biomes = biomeGenerator.generateBiomes(dimension.width, dimension.depth)
    val blocks = ObjectMutableSpace(dimension, { Block(AIR) })
    generationPopulator.populate(blocks, biomes)
    populators.forEach { populator ->
      populator.populate(blocks, biomes)
    }
    return blocks
  }
}