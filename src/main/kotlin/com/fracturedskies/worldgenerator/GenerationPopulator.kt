package com.fracturedskies.worldgenerator

import com.fracturedskies.api.block.Block
import com.fracturedskies.engine.collections.*


interface GenerationPopulator {
  fun populate(blocks: ObjectMutableSpace<Block>, biomes: ObjectArea<Biome>)
}