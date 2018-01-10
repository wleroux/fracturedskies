package com.fracturedskies.engine.collections

import com.fracturedskies.engine.math.Vector3i


open class BooleanMap(private val dimension: Dimension) {
  private val backend = BooleanArray(dimension.size)
  operator fun get(pos: Vector3i) = get(pos.x, pos.y, pos.z)
  operator fun get(x: Int, y: Int, z: Int) = backend[dimension(x, y, z)]
  operator fun set(pos: Vector3i, value: Boolean) = set(pos.x, pos.y, pos.z, value);
  operator fun set(x: Int, y: Int, z: Int, value: Boolean) {
    backend[dimension.invoke(x, y, z)] = value
  }
  fun clear() {
    backend.fill(false)
  }
}
open class Vector3iSet: Iterable<Vector3i> {
  private val backend = hashSetOf<Vector3i>()
  operator fun get(x: Int, y: Int, z: Int) = get(Vector3i(x, y, z))
  operator fun get(pos: Vector3i) = backend.contains(pos)
  operator fun set(x: Int, y: Int, z: Int, value: Boolean) = set(Vector3i(x, y, z), value)
  operator fun set(pos: Vector3i, value: Boolean) {
    if (value) backend.add(pos) else backend.remove(pos)
  }
  override fun iterator() = backend.iterator()
  fun first(): Vector3i = backend.first()
  fun clear() {
    backend.clear()
  }

  fun isEmpty() = backend.isEmpty()
}
open class ByteMap(private val dimension: Dimension) {
  private val backend = ByteArray(dimension.size)
  operator fun get(pos: Vector3i) = get(pos.x, pos.y, pos.z)
  operator fun get(x: Int, y: Int, z: Int) = backend[dimension(x, y, z)]
  operator fun set(pos: Vector3i, value: Byte) = set(pos.x, pos.y, pos.z, value)
  operator fun set(x: Int, y: Int, z: Int, value: Byte) {
    backend[dimension(x, y, z)] = value
  }
  fun clear() {
    backend.fill(0.toByte())
  }
}
open class IntMap(private val dimension: Dimension) {
  private val backend = IntArray(dimension.size)
  operator fun get(pos: Vector3i) = get(pos.x, pos.y, pos.z)
  operator fun get(x: Int, y: Int, z: Int) = backend[dimension(x, y, z)]
  operator fun set(pos: Vector3i, value: Int) = set(pos.x, pos.y, pos.z, value)
  operator fun set(x: Int, y: Int, z: Int, value: Int) {
    backend[dimension(x, y, z)] = value
  }
  fun clear() {
    backend.fill(0)
  }
}
open class ObjectMap<K>(val dimension: Dimension, private val backend: Array<K>) {
  companion object {
    inline operator fun <reified K> invoke(dimension: Dimension, noinline init: (Int) -> K): ObjectMap<K> {
      return ObjectMap(dimension, Array(dimension.size, init))
    }
  }

  val width get() = dimension.width
  val height get() = dimension.height
  val depth get() = dimension.depth

  operator fun get(index: Int) = backend[index]
  operator fun set(index: Int, value: K) {
    backend[index] = value
  }
  fun has(index: Int) = dimension.has(index)

  operator fun get(pos: Vector3i) = get(dimension(pos))
  operator fun set(pos: Vector3i, value: K) = set(dimension(pos), value)
  fun has(pos: Vector3i) = dimension.has(pos)

  operator fun get(x: Int, y: Int, z: Int) = get(dimension(x, y, z))
  operator fun set(x: Int, y: Int, z: Int, value: K) = set(dimension(x, y, z), value)
  fun has(x: Int, y: Int, z: Int) = dimension.has(x, y, z)
}