package com.fracturedskies.engine.collections

import com.fracturedskies.engine.math.Vector3i
import org.lwjgl.BufferUtils
import java.util.*
import java.util.AbstractMap.SimpleEntry
import kotlin.collections.ArrayList

interface Space<out K>: HasDimension {
  operator fun get(index: Int): K
  operator fun get(pos: Vector3i) = get(index(pos))
  operator fun get(x: Int, y: Int, z: Int) = get(index(x, y, z))
}

inline fun <K> (Space<K>).forEach(action: (Map.Entry<Vector3i, K>) -> Unit) {
  this.dimension.indices().forEach { index ->
    action(SimpleEntry(this.vector3i(index), this[index]))
  }
}
inline fun <K> (Space<K>).forEach(action: (Vector3i, K) -> Unit) {
  this.dimension.indices().forEach { index ->
    action(this.vector3i(index), this[index])
  }
}

inline fun <K, R> (Space<K>).flatMap(crossinline action: (Map.Entry<Vector3i, K>) -> Iterable<R>): List<R> {
  val result = ArrayList<R>()
  forEach { key, value ->
    action(SimpleEntry(key, value))
        .toCollection(result)
  }
  return result
}

inline fun <K, R> (Space<K>).map(crossinline action: (Vector3i, K) -> R): List<R> {
  val result = ArrayList<R>(this@map.dimension.size)
  forEach { key, value ->
    result.add(action(key, value))
  }
  return result
}

fun <K, R> (Space<K>).project(mapper: (K) -> R): Space<R> {
  return object : Space<R> {
    override val dimension: Dimension = this@project.dimension
    override fun get(index: Int): R = mapper(this@project.get(index))
  }
}



interface MutableSpace<K>: Space<K> {
  operator fun set(index: Int, value: K)
  operator fun set(pos: Vector3i, value: K) = set(index(pos), value)
  operator fun set(x: Int, y: Int, z: Int, value: K) = set(index(x, y, z), value)
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