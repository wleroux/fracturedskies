package com.fracturedskies.engine.math

import org.lwjgl.stb.STBPerlin

fun <T: Comparable<T>> clamp(value: T, range: ClosedRange<T>) = when {
  value < range.start -> range.start
  value > range.endInclusive -> range.endInclusive
  else -> value
}

fun map(value: Float, original: ClosedRange<Float>, target: ClosedRange<Float>): Float {
  val alpha = (value - original.start) / (original.endInclusive - original.start)
  return lerp(alpha, target)
}

fun lerp(alpha: Float, target: ClosedRange<Float>): Float =
  target.start + clamp(alpha, 0f..1f) * (target.endInclusive - target.start)
fun lerp(alpha: Float, target: IntRange): Int = target.start + (clamp(alpha, 0f..1f) * (target.endInclusive - target.start)).toInt()
fun lerp(alpha: Float, start: Color4, end: Color4) =
    Color4(
        lerp(alpha, start.red until end.red),
        lerp(alpha, start.green until end.green),
        lerp(alpha, start.blue until end.blue),
        lerp(alpha, start.alpha until end.alpha)
    )

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
