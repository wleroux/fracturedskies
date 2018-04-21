package com.fracturedskies.engine.collections

import com.fracturedskies.engine.math.Vector2i


interface Area<out K>: Iterable<Pair<Int, K>> {
  val width: Int
  val depth: Int
  private val size get() = width * depth

  operator fun get(index: Int): K
  operator fun get(pos: Vector2i) = get(pos.x, pos.z)
  operator fun get(x: Int, z: Int) = get(x * depth + z)
  fun has(index: Int) = 0 <= index && index < size
  fun has(pos: Vector2i) = has(pos.x, pos.z)
  fun has(x: Int, z: Int) = 0 <= x && x < width && 0 <= z && z < depth
  override fun iterator(): Iterator<Pair<Int, K>> {
    val indexIterator = (0 until size).iterator()
    return object : Iterator<Pair<Int, K>> {
      override fun hasNext() = indexIterator.hasNext()
      override fun next(): Pair<Int, K> {
        val index = indexIterator.nextInt()
        return index to this@Area[index]
      }
    }
  }
}
interface MutableArea<K>: Area<K> {
  operator fun set(index: Int, value: K)
  operator fun set(pos: Vector2i, value: K) = set(pos.x, pos.z, value)
  operator fun set(x: Int, z: Int, value: K) = set(x * depth + z, value)
}

open class ObjectArea<K>(final override val width: Int, final override val depth: Int, init: (Int) -> K): Area<K> {
  @Suppress("UNCHECKED_CAST")
  protected val backend = Array(width * depth, { init(it) as Any? }) as Array<K>

  @Suppress("UNCHECKED_CAST")
  override operator fun get(index: Int) = backend[index]
}
open class ObjectMutableArea<K>(width: Int, depth: Int, init: (Int) -> K): ObjectArea<K>(width, depth, init), MutableArea<K> {
  override operator fun set(index: Int, value: K) { backend[index] = value }
}