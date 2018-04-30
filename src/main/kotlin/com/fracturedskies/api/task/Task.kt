package com.fracturedskies.api.task

import com.fracturedskies.api.task.TaskPriority.AVERAGE
import com.fracturedskies.engine.Id


class Task(
    val id: Id,
    val details: TaskType,
    var priority: TaskPriority = AVERAGE,
    var assigned: List<Id>
) {
  override fun hashCode() = id.hashCode()
  override fun equals(other: Any?) = when(other) {
    is Task -> id == other.id
    else -> false
  }

  override fun toString(): String {
    return "Task[id=$id; details=$details; priority=$priority; assigned=$assigned]"
  }
}