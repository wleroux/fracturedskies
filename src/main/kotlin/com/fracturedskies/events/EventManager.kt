package com.fracturedskies.events

typealias EventListener = (Event) -> Unit
object EventManager {

  private val listeners = mutableListOf<EventListener>()

  fun registerListener(listener: EventListener) {
    listeners.add(listener)
  }

  fun <T: Event> post(event: T) : Boolean {
    listeners.forEach({it -> it(event)})
    return !event.isCancelled()
  }
}