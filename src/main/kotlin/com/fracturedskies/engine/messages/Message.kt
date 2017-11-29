package com.fracturedskies.engine.messages

import com.fracturedskies.engine.collections.Context

interface Message {
  val cause: Cause
  val context: Context
}

class Cause private constructor(private val objects: List<Any>): Sequence<Any> {
  override fun iterator() = objects.listIterator()
  override fun toString() = objects.joinToString(prefix = "[", postfix = "]")
  companion object {
    fun of(obj: Any, vararg objects: Any) = Cause(listOf(obj, *objects))
    fun of(cause: Cause, vararg objects: Any): Cause {
      val newObjects = cause.objects.toMutableList()
      newObjects.addAll(objects)
      return Cause(newObjects)
    }
  }
}