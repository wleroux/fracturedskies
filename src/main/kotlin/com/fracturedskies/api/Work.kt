package com.fracturedskies.api

interface Work {
  val type: WorkType
  val priority: Int
}

enum class WorkType {
  CONSTRUCTION
}
