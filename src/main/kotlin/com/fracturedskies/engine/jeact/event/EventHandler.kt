package com.fracturedskies.engine.jeact.event

import kotlin.reflect.KClass

typealias EventHandler = (Event) -> Unit

class EventHandlers(private val handlers: List<EventHandler>): EventHandler {
  companion object {
    operator fun invoke(vararg handlers: EventHandler): EventHandlers {
      return EventHandlers(listOf(*handlers))
    }
  }
  override fun invoke(event: Event) {
    for (handler in handlers) {
      handler(event)
      if (event.stopPropogation)
        return
    }
  }
}

@Suppress("UNCHECKED_CAST")
fun <T: Event> on(type: KClass<T>, phase: Phase = Phase.BUBBLE, handler: (T) -> Unit): EventHandler {
  return { event ->
    if (event.phase === phase && type.isInstance(event)) {
      handler(event as T)
    }
  }
}