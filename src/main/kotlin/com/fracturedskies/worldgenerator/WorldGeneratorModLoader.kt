package com.fracturedskies.worldgenerator

import com.fracturedskies.api.*
import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.math.Vector3i
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.register
import com.fracturedskies.engine.messages.MessageBus.send
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

class WorldGeneratorModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(MessageChannel(initialContext) { message ->
      if (message is NewGameRequested) {
        val random = Random().apply { setSeed(message.seed.toLong()) }
        val worldGenerator = WorldGenerator(
            DefaultBiomeGenerator(),
            DefaultGenerationPopulator(message.seed),
            listOf(BasicTreePopulator(Random(random.nextLong())))
        )

        val generatedWorld = worldGenerator.generate(message.dimension)
        send(WorldGenerated(Vector3i(0, 0, 0), generatedWorld, Cause.of(this)))
      }
    })
  }
}