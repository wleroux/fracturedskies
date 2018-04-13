package com.fracturedskies.render

import com.fracturedskies.api.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.render.world.WorldState
import com.fracturedskies.render.world.WorldState.Block

data class GameState(
    val initialized: Boolean = false,
    val world: WorldState? = null
) {
  fun process(message: Any): GameState {
    return when (message) {
      is NewGameRequested -> this.copy(
          initialized = true,
          world = WorldState(
              ChunkSpace(message.dimension, { ObjectSpace(CHUNK_DIMENSION, { Block() }) }),
              kotlin.collections.mapOf(),
              kotlin.collections.mapOf(),
              0f
          )
      )
      else -> {
        if (initialized) {
          this.copy(world = world!!.process(message))
        } else {
          this
        }
      }
    }
  }
}