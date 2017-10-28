package com.fracturedskies.events

import kotlin.reflect.KClass

interface Event {
  val cause: Cause
  val context: Context
}
interface Cancellable {
  var cancelled: Boolean
}
fun Event.isCancelled(): Boolean = this is Cancellable && this.cancelled

class Cause private constructor(private val objects: List<Any>): Sequence<Any> {
  override fun iterator(): ListIterator<Any> {
    return objects.listIterator()
  }
  override fun toString(): String {
    return objects.joinToString(prefix = "[", postfix = "]")
  }
  companion object {
    fun of(obj: Any, vararg objects: Any): Cause {
      return Cause(listOf(obj, *objects))
    }

    fun of(cause: Cause, vararg objects: Any): Cause {
      val newObjects = cause.objects.toMutableList()
      newObjects.addAll(objects)
      return Cause(newObjects)
    }
  }
}

data class Context(private val entries: Map<ContextKey<*>, *>) {
  fun <T: Any> get(key: ContextKey<T>): T {
    @Suppress("UNCHECKED_CAST")
    return entries[key] as T
  }
  override fun toString(): String {
    return entries.toString()
  }
  companion object {
    fun <A: Any> of(property1: Pair<ContextKey<A>, A>): Context {
      return Context(mutableMapOf(property1).toMap())
    }
    fun <A: Any, B: Any> of(property1: Pair<ContextKey<A>, A>, property2: Pair<ContextKey<B>, B>): Context {
      return Context(mutableMapOf(property1, property2).toMap())
    }
    fun <A: Any> of(context: Context, additionalProperty1: Pair<ContextKey<A>, A>): Context {
      val newContext = context.entries.toMutableMap()
      newContext.put(additionalProperty1.first, additionalProperty1.second)
      return Context(newContext.toMap())
    }

    fun empty(): Context {
      return Context(mapOf<ContextKey<*>, Any>())
    }
  }
}
data class ContextKey<T: Any>(val id: String, val type: KClass<T>) {
  override fun toString(): String {
    return "$id: ${type.simpleName}"
  }
}