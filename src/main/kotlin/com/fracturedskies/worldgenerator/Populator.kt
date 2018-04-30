package com.fracturedskies.worldgenerator

import com.fracturedskies.api.block.Block
import com.fracturedskies.engine.collections.*


interface Populator {
  fun populate(blocks: ObjectMutableSpace<Block>, biomes: ObjectArea<Biome>)
}