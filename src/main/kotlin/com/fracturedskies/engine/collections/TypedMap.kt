package com.fracturedskies.engine.collections

class TypedMap(private val delegate: Map<TypedKey<*>, Any>) {
  companion object {
    operator fun invoke(vararg values: Pair<TypedKey<*>, Any>) =TypedMap(mapOf(*values))
  }

  val entries: Set<Map.Entry<TypedKey<*>, Any>> = delegate.entries
  val keys: Set<TypedKey<*>> = delegate.keys
  val size: Int = delegate.size
  val values: Collection<Any> = delegate.values
  fun containsKey(key: TypedKey<*>): Boolean = delegate.containsKey(key)
  fun containsValue(value: Any): Boolean = delegate.containsValue(value)
  fun isEmpty(): Boolean = delegate.isEmpty()
  @Suppress("UNCHECKED_CAST") operator fun <T> get(key: TypedKey<T>): T? = delegate[key] as T?

  override fun toString(): String {
    return entries.map({(key, value) -> "$key=$value"}).joinToString(prefix = "{", separator = ", ", postfix = "}")
  }
}

data class TypedKey<T>(private val label: String) {
  override fun toString() = label
}
