package com.fracturedskies

import com.fracturedskies.events.*

fun main(args: Array<String>) {
  EventManager.registerListener { event ->
    if (!event.isCancelled()) {
      System.out.println("Event: $event")
    }
  }

  val newGameRequest = NewGameRequested("Game 1", Cause.of("player"), Context.of(SOURCE to "source"))
  if (EventManager.post(newGameRequest)) {
    System.out.println("Creating new game: " + newGameRequest.name)
  }
}