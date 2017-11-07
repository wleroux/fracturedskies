package com.fracturedskies.engine.math

import org.lwjgl.stb.STBPerlin

fun map(value: Float, original: ClosedRange<Float>, target: ClosedRange<Float>): Float {
  val alpha = (value - original.start) / (original.endInclusive - original.start)
  return lerp(alpha, target)
}

fun lerp(alpha: Float, target: ClosedRange<Float>): Float {
  return when {
    alpha < 0f -> target.start
    alpha > 1f -> target.endInclusive
    else -> target.start + alpha * (target.endInclusive - target.start)
  }
}

/**
 * Perlin Noise
 */
fun noise3(x: Float, y: Float, z: Float, x_wrap: Int = 0, y_wrap: Int = 0, z_wrap: Int = 0): Float {
  return STBPerlin.stb_perlin_noise3(x, y, z, x_wrap, y_wrap, z_wrap)
}

/**
 * Fractional Brownian Motion using Perlin Noise
 */
fun fbm_noise3(x: Float, y: Float, z: Float, lacunarity: Float = 2.0f, gain: Float = 0.5f, octaves: Int = 6, x_wrap: Int = 0, y_wrap: Int = 0, z_wrap: Int = 0): Float {
  return STBPerlin.stb_perlin_fbm_noise3(x, y, z, lacunarity, gain, octaves, x_wrap, y_wrap, z_wrap)
}
