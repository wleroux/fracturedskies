package com.fracturedskies.colonist

import com.fracturedskies.engine.Id

enum class Priority {
  LOWEST,
  LOWER,
  LOW,
  BELOW_AVERAGE,
  AVERAGE,
  ABOVE_AVERAGE,
  HIGH,
  HIGHER,
  HIGHEST
}
enum class Category {
  CONSTRUCTION,
  MINE
}
data class Task<out T>(
    val id: Id,
    val category: Category,
    val priority: Priority = Priority.AVERAGE,
    val condition: Condition,
    val assigned: List<Id>,
    val taskDetails: T
)
