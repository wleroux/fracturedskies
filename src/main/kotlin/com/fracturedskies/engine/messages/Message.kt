package com.fracturedskies.engine.messages

import com.fracturedskies.engine.collections.Context

interface Message {
  val cause: Cause
  val context: Context
}

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