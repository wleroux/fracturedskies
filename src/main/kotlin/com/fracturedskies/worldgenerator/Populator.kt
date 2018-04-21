package com.fracturedskies.worldgenerator

import com.fracturedskies.Block
import com.fracturedskies.engine.collections.*


interface Populator {
  fun populate(blocks: ObjectMutableSpace<Block>, biomes: ObjectArea<Biome>)
}