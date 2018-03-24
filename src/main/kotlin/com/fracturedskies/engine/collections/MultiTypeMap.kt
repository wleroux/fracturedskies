package com.fracturedskies.engine.collections

typealias TypedEntry<A> = Pair<TypedKey<A>, A>

data class TypedKey<T>(private val label: Any) {
  override fun toString() = label.toString()
}

class MultiTypeMap(private val delegate: Map<TypedKey<*>, *>) {
  companion object {
    @Suppress("UNCHECKED_CAST") operator fun invoke() = MultiTypeMap(mapOf<TypedKey<*>, Any>())
    @Suppress("UNCHECKED_CAST") operator fun <A> invoke(value1: TypedEntry<A>) = MultiTypeMap(mapOf(value1))
    @Suppress("UNCHECKED_CAST") operator fun <A, B> invoke(value1: TypedEntry<A>, value2: TypedEntry<B>) = MultiTypeMap(mapOf(value1, value2))
    @Suppress("UNCHECKED_CAST") operator fun <A, B, C> invoke(value1: TypedEntry<A>, value2: TypedEntry<B>, value3: TypedEntry<C>) = MultiTypeMap(mapOf(
            value1, value2, value3
    ))
    @Suppress("UNCHECKED_CAST") operator fun <A, B, C, D> invoke(value1: TypedEntry<A>, value2: TypedEntry<B>, value3: TypedEntry<C>, value4: TypedEntry<D>) = MultiTypeMap(mapOf(
            value1, value2, value3, value4
    ))
    @Suppress("UNCHECKED_CAST") operator fun <A, B, C, D, E> invoke(value1: TypedEntry<A>, value2: TypedEntry<B>, value3: TypedEntry<C>, value4: TypedEntry<D>, value5: TypedEntry<E>) = MultiTypeMap(mapOf(
        value1, value2, value3, value4, value5
    ))
    @Suppress("UNCHECKED_CAST") operator fun <A, B, C, D, E, F> invoke(value1: TypedEntry<A>, value2: TypedEntry<B>, value3: TypedEntry<C>, value4: TypedEntry<D>, value5: TypedEntry<E>, value6: TypedEntry<F>) = MultiTypeMap(mapOf(
        value1, value2, value3, value4, value5, value6
    ))
    @Suppress("UNCHECKED_CAST") operator fun <A, B, C, D, E, F, G> invoke(value1: TypedEntry<A>, value2: TypedEntry<B>, value3: TypedEntry<C>, value4: TypedEntry<D>, value5: TypedEntry<E>, value6: TypedEntry<F>, value7: TypedEntry<G>) = MultiTypeMap(mapOf(
        value1, value2, value3, value4, value5, value6, value7
    ))
  }

  val entries: Set<Map.Entry<TypedKey<*>, *>> = delegate.entries
  val keys: Set<TypedKey<*>> = delegate.keys
  val size: Int = delegate.size
  val values: Collection<*> = delegate.values
  fun containsKey(key: TypedKey<*>): Boolean = delegate.containsKey(key)
  fun containsValue(value: Any): Boolean = delegate.containsValue(value)
  fun isEmpty(): Boolean = delegate.isEmpty()
  @Suppress("UNCHECKED_CAST") operator fun <T> get(key: TypedKey<T>): T? = delegate[key] as T?

  override fun toString(): String {
    return entries.joinToString(prefix = "{", separator = ", ", postfix = "}")
  }
  override fun hashCode() = delegate.hashCode()
  override fun equals(other: Any?): Boolean {
    if (this === other)
      return true
    when (other) {
      is MultiTypeMap -> {
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

  fun with(context: MultiTypeMap): MultiTypeMap {
    return MultiTypeMap(delegate.toMutableMap().apply({
      putAll(context.delegate)
    }))
  }

  fun <A> with(value1: TypedEntry<A>) = MultiTypeMap(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
  }))
  fun <A, B> with(value1: TypedEntry<A>, value2: TypedEntry<B>) = MultiTypeMap(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
    put(value2.first, value2.second)
  }))
  fun <A, B, C> with(value1: TypedEntry<A>, value2: TypedEntry<B>, value3: TypedEntry<C>) = MultiTypeMap(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
    put(value2.first, value2.second)
    put(value3.first, value3.second)
  }))
  fun <A, B, C, D> with(value1: TypedEntry<A>, value2: TypedEntry<B>, value3: TypedEntry<C>, value4: TypedEntry<D>) = MultiTypeMap(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
    put(value2.first, value2.second)
    put(value3.first, value3.second)
    put(value4.first, value4.second)
  }))
  fun <A, B, C, D, E> with(value1: TypedEntry<A>, value2: TypedEntry<B>, value3: TypedEntry<C>, value4: TypedEntry<D>, value5: TypedEntry<E>) = MultiTypeMap(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
    put(value2.first, value2.second)
    put(value3.first, value3.second)
    put(value4.first, value4.second)
    put(value5.first, value5.second)
  }))
  fun <A, B, C, D, E, F> with(value1: TypedEntry<A>, value2: TypedEntry<B>, value3: TypedEntry<C>, value4: TypedEntry<D>, value5: TypedEntry<E>, value6: TypedEntry<F>) = MultiTypeMap(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
    put(value2.first, value2.second)
    put(value3.first, value3.second)
    put(value4.first, value4.second)
    put(value5.first, value5.second)
    put(value6.first, value6.second)
  }))
  fun <A, B, C, D, E, F, G> with(value1: TypedEntry<A>, value2: TypedEntry<B>, value3: TypedEntry<C>, value4: TypedEntry<D>, value5: TypedEntry<E>, value6: TypedEntry<F>, value7: TypedEntry<G>) = MultiTypeMap(delegate.toMutableMap().apply({
    put(value1.first, value1.second)
    put(value2.first, value2.second)
    put(value3.first, value3.second)
    put(value4.first, value4.second)
    put(value5.first, value5.second)
    put(value6.first, value6.second)
    put(value7.first, value7.second)
  }))
}
