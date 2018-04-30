package com.fracturedskies.api

import com.fracturedskies.engine.collections.Dimension


enum class GameSize(val dimension: Dimension) {
  MINI(Dimension(CHUNK_X_SIZE, 16 * CHUNK_Y_SIZE, CHUNK_Z_SIZE)), // 16 chunks, 65536 blocks
  SMALL(Dimension(4 * CHUNK_X_SIZE, 16 * CHUNK_Y_SIZE, 4 * CHUNK_Z_SIZE)), // 256 chunks, 1048576 blocks
  NORMAL(Dimension(8 * CHUNK_X_SIZE, 16 * CHUNK_Y_SIZE, 8 * CHUNK_Z_SIZE)), // 1024 chunks, 4194304 blocks
  LARGE(Dimension(12 * CHUNK_X_SIZE, 16 * CHUNK_Y_SIZE, 12 * CHUNK_Z_SIZE)), // 2304 chunks, 9437184 blocks
  MEGA(Dimension(16 * CHUNK_X_SIZE, 16 * CHUNK_Y_SIZE, 16 * CHUNK_Z_SIZE)) // 4096 chunks, 16777216 blocks
}
