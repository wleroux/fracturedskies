package com.fracturedskies.engine.collections

import com.fracturedskies.engine.math.Vector3i
import org.lwjgl.BufferUtils
import java.util.*

interface Space<out K>: Iterable<Pair<Int, K>> {
  val dimension: Dimension
  val width get() = dimension.width
  val height get() = dimension.height
  val depth get() = dimension.depth
  operator fun get(index: Int): K
  operator fun get(pos: Vector3i) = get(dimension(pos))
  operator fun get(x: Int, y: Int, z: Int) = get(dimension(x, y, z))
  fun has(index: Int) = dimension.has(index)
  fun has(pos: Vector3i) = has(pos.x, pos.y, pos.z)
  fun has(x: Int, y: Int, z: Int) = dimension.has(x, y, z)
  override fun iterator(): Iterator<Pair<Int, K>> {
    val indexIterator = dimension.indices().iterator()
    return object : Iterator<Pair<Int, K>> {
      override fun hasNext() = indexIterator.hasNext()
      override fun next(): Pair<Int, K> {
        val index = indexIterator.nextInt()
        return index to this@Space[index]
      }
    }
  }
}
interface MutableSpace<K>: Space<K> {
  operator fun set(index: Int, value: K)
  operator fun set(pos: Vector3i, value: K) = set(dimension(pos), value)
  operator fun set(x: Int, y: Int, z: Int, value: K) = set(dimension(x, y, z), value)
}

open class BooleanSpace(final override val dimension: Dimension, init: (Int) -> Boolean = { false }): Space<Boolean> {
  protected val backend = BitSet(dimension.size).apply {
    dimension.forEach { index -> set(index, init(index)) }
  }
  override fun get(index: Int) = backend[index]
}
open class BooleanMutableSpace(dimension: Dimension, init: (Int) -> Boolean = { false }): BooleanSpace(dimension, init), MutableSpace<Boolean> {
  override operator fun set(index: Int, value: Boolean) { backend[index] = value }
  fun clear() { backend.clear(0, backend.length())}
}

open class ByteSpace(final override val dimension: Dimension, init: (Int) -> Byte = { 0.toByte() }): Space<Byte> {
  protected val backend = requireNotNull(BufferUtils.createByteBuffer(dimension.size).apply {
    dimension.forEach { index -> put(index, init(index) ) }
  })
  override fun get(index: Int) = backend[index]
}
open class ByteMutableSpace(dimension: Dimension, init: (Int) -> Byte = { 0.toByte() }): ByteSpace(dimension, init), MutableSpace<Byte> {
  override operator fun set(index: Int, value: Byte) { backend.put(index, value) }
  fun clear() { BufferUtils.zeroBuffer(backend) }
}

open class IntSpace(final override val dimension: Dimension, init: (Int) -> Int = { 0 }): Space<Int> {
  protected val backend = IntArray(dimension.size, init)
  override fun get(index: Int) = backend[index]
}
open class IntMutableSpace(dimension: Dimension, init: (Int) -> Int = { 0 }): IntSpace(dimension, init), MutableSpace<Int> {
  override operator fun set(index: Int, value: Int) { backend[index] = value }
  fun clear() { backend.fill(0, 0, backend.size) }
}

open class ObjectSpace<K>(final override val dimension: Dimension, init: (Int) -> K): Space<K> {
  @Suppress("UNCHECKED_CAST")
  protected val backend = Array(dimension.size, { init(it) as Any? }) as Array<K>

  @Suppress("UNCHECKED_CAST")
  override operator fun get(index: Int) = backend[index]
}
open class ObjectMutableSpace<K>(dimension: Dimension, init: (Int) -> K): ObjectSpace<K>(dimension, init), MutableSpace<K> {
  override operator fun set(index: Int, value: K) { backend[index] = value }
}