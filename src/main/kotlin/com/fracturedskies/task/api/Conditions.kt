package com.fracturedskies.task.api

import com.fracturedskies.*
import com.fracturedskies.engine.Id


interface Condition {
  fun matches(world: World, colonist: Colonist, task: Task): Boolean
}

class AllOfCondition(private vararg val conditions: Condition): Condition {
  override fun matches(world: World, colonist: Colonist, task: Task) = conditions.all { it.matches(world, colonist, task) }
  override fun toString() = "all of ${conditions.joinToString(prefix = "[", separator = ",", postfix = "]")}"
}

class AnyOfCondition(private vararg val conditions: Condition): Condition {
  override fun matches(world: World, colonist: Colonist, task: Task) = conditions.any { it.matches(world, colonist, task) }
  override fun toString() = "any of ${conditions.joinToString(prefix = "[", separator = ",", postfix = "]")}"
}

object NoneCondition: Condition {
  override fun matches(world: World, colonist: Colonist, task: Task) = false
  override fun toString() = "none"
}

object AllCondition: Condition {
  override fun matches(world: World, colonist: Colonist, task: Task) = true
  override fun toString() = "all"
}

object SingleAssigneeCondition: Condition {
  override fun matches(world: World, colonist: Colonist, task: Task) =
      task.assigned.contains(colonist.id) || task.assigned.isEmpty()

  override fun toString() = "only one assignee"
}

class SpecificColonistCondition(val id: Id): Condition {
  override fun matches(world: World, colonist: Colonist, task: Task) =
      id == colonist.id

  override fun toString() = "Colonist must be $id"
}

class IsPossible: Condition {
  override fun matches(world: World, colonist: Colonist, task: Task): Boolean {
    return true
  }
}