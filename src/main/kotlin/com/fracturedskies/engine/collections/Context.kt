package com.fracturedskies.engine.collections

typealias ContextEntry<A> = Pair<Key<A>, A>
class Context(private val delegate: Map<Key<*>, *>) {
  companion object {
    @Suppress("UNCHECKED_CAST") operator fun invoke() = Context(mapOf<Key<*>, Any>())
    @Suppress("UNCHECKED_CAST") operator fun <A> invoke(value1: ContextEntry<A>) = Context(mapOf(value1))
    @Suppress("UNCHECKED_CAST") operator fun <A, B> invoke(value1: ContextEntry<A>, value2: ContextEntry<B>) = Context(mapOf(value1, value2))
    @Suppress("UNCHECKED_CAST") operator fun <A, B, C> invoke(value1: ContextEntry<A>, value2: ContextEntry<B>, value3: ContextEntry<C>) = Context(mapOf(
            value1, value2, value3
    ))
    @Suppress("UNCHECKED_CAST") operator fun <A, B, C, D> invoke(value1: ContextEntry<A>, value2: ContextEntry<B>, value3: ContextEntry<C>, value4: ContextEntry<D>) = Context(mapOf(
            value1, value2, value3, value4
    ))
    @Suppress("UNCHECKED_CAST") operator fun <A, B, C, D, E> invoke(value1: ContextEntry<A>, value2: ContextEntry<B>, value3: ContextEntry<C>, value4: ContextEntry<D>, value5: ContextEntry<E>) = Context(mapOf(
            value1, value2, value3, value4, value5
    ))
  }

  val entries: Set<Map.Entry<Key<*>, *>> = delegate.entries
  val keys: Set<Key<*>> = delegate.keys
  val size: Int = delegate.size
  val values: Collection<*> = delegate.values
  fun containsKey(key: Key<*>): Boolean = delegate.containsKey(key)
  fun containsValue(value: Any): Boolean = delegate.containsValue(value)
  fun isEmpty(): Boolean = delegate.isEmpty()
  @Suppress("UNCHECKED_CAST") operator fun <T> get(key: Key<T>): T? = delegate[key] as T?

  override fun toString(): String {
    return entries.joinToString(prefix = "{", separator = ", ", postfix = "}")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other)
      return true
    when (other) {
      is Context -> {
        if (this.size != other.size) {
          return false
        }

        for ((key, value) in this.entries) {
          if (other[key] != value) {
            return false
          }
        }
        return true
      }
      else -> return false
    }
  }

  fun <A> with(value1: ContextEntry<A>) = Context(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
  }))
  fun <A, B> with(value1: ContextEntry<A>, value2: ContextEntry<B>) = Context(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
    put(value2.first, value2.second)
  }))
  fun <A, B, C> with(value1: ContextEntry<A>, value2: ContextEntry<B>, value3: ContextEntry<C>) = Context(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
    put(value2.first, value2.second)
    put(value3.first, value3.second)
  }))
  fun <A, B, C, D> with(value1: ContextEntry<A>, value2: ContextEntry<B>, value3: ContextEntry<C>, value4: ContextEntry<D>) = Context(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
    put(value2.first, value2.second)
    put(value3.first, value3.second)
    put(value4.first, value4.second)
  }))
  fun <A, B, C, D, E> with(value1: ContextEntry<A>, value2: ContextEntry<B>, value3: ContextEntry<C>, value4: ContextEntry<D>, value5: ContextEntry<E>) = Context(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
    put(value2.first, value2.second)
    put(value3.first, value3.second)
    put(value4.first, value4.second)
    put(value5.first, value5.second)
  }))
}

data class Key<T>(private val label: String) {
  override fun toString() = label
}
