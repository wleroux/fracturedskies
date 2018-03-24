package com.fracturedskies.engine.collections

import com.fracturedskies.engine.math.Vector3i

open class Vector3iMutableSet: Iterable<Vector3i> {
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
