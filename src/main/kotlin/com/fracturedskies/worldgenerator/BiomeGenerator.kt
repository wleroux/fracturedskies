package com.fracturedskies.worldgenerator

import com.fracturedskies.engine.collections.ObjectArea


interface BiomeGenerator {
  fun generateBiomes(width: Int, depth: Int): ObjectArea<Biome>
}