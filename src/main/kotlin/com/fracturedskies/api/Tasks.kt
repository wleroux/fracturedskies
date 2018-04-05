package com.fracturedskies.api

import com.fracturedskies.engine.math.Vector3i


data class PlaceBlock(val pos: Vector3i, val blockType: BlockType)
data class RemoveBlock(val pos: Vector3i)