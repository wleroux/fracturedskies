package com.fracturedskies.game.workers

abstract class Work(val type: WorkType, val priority: Int) {
  abstract operator fun invoke()
}

enum class WorkType {
  CONSTRUCTION
}