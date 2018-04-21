package com.fracturedskies.worldgenerator

import com.fracturedskies.engine.collections.ObjectArea


class DefaultBiomeGenerator(): BiomeGenerator {
  override fun generateBiomes(width: Int, depth: Int): ObjectArea<Biome> {
    return ObjectArea(width, depth, {ForestBiome})
  }
}