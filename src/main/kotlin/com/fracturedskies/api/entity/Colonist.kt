package com.fracturedskies.api.entity

import com.fracturedskies.api.task.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.math.Vector3i


class Colonist(
    val id: Id,
    var position: Vector3i,
    var direction: Vector3i,
    val categoryPriorities: MutableMap<TaskCategory, TaskPriority>,
    val rejectedTasks: MutableMap<Id, Int>,
    val inventory: MutableList<Id>,
    var assignedTask: Id?
) {
  override fun hashCode() = id.hashCode()
  override fun equals(other: Any?) = when (other) {
    is Colonist -> id == other.id
    else -> false
  }
}
