package com.fracturedskies.worldgenerator

import com.fracturedskies.api.*
import com.fracturedskies.engine.api.Cause
import java.util.*
import javax.enterprise.event.Observes
import javax.inject.Inject

class WorldGeneratorSystem {

  @Inject
  lateinit var world: World

  fun onNewGameRequested(@Observes message: NewGameRequested) {
    val random = Random().apply { setSeed(message.seed.toLong()) }
    val worldGenerator = WorldGenerator(
        DefaultBiomeGenerator(),
        DefaultGenerationPopulator(message.seed),
        listOf(BasicTreePopulator(Random(random.nextLong())))
    )

    val generatedWorld = worldGenerator.generate(message.dimension)
    world.generateWorld(generatedWorld, Cause.of(this))
  }
}