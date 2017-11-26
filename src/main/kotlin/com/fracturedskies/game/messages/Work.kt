package com.fracturedskies.game.messages

interface Work {
  val type: WorkType
  val priority: Int
}

enum class WorkType {
  CONSTRUCTION
}
