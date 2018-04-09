package com.fracturedskies.task.api

import com.fracturedskies.task.*
import com.fracturedskies.task.api.TaskPriority.AVERAGE

interface PriorityComparator {
  fun compare(colonist: Colonist, o1: Task<*>, o2: Task<*>): Int
}

fun colonistPriorityComparator(state: WorldState) = CompoundPriorityComparator(
    CategoryPriorityComparator,
    TaskPriorityComparator,
    FocusBiasPriorityComparator,
    RejectionComparator(state),
    CostComparator(state)
)

class RejectionComparator(private val state: WorldState): PriorityComparator {
  override fun compare(colonist: Colonist, o1: Task<*>, o2: Task<*>): Int {
    val rejected1 = colonist.rejectedTasks[o1.id] ?: 0
    val rejected2 = colonist.rejectedTasks[o2.id] ?: 0
    return -rejected1.compareTo(rejected2)
  }
}

class CostComparator(private val state: WorldState): PriorityComparator {
  override fun compare(colonist: Colonist, o1: Task<*>, o2: Task<*>): Int {
    val cost1 = o1.behavior.cost(state, colonist)
    val cost2 = o2.behavior.cost(state, colonist)
    return -cost1.compareTo(cost2)
  }
}

class CompoundPriorityComparator(private vararg val priorityComparators: PriorityComparator): PriorityComparator {
  override fun compare(colonist: Colonist, o1: Task<*>, o2: Task<*>): Int {
    for (priorityComparator in priorityComparators) {
      val comparison = priorityComparator.compare(colonist, o1, o2)
      if (comparison != 0)
        return comparison
    }
    return 0
  }
}

object CategoryPriorityComparator: PriorityComparator {
  override fun compare(colonist: Colonist, o1: Task<*>, o2: Task<*>): Int {
    val o1Priority = colonist.categoryPriorities[o1.category] ?: AVERAGE
    val o2Priority = colonist.categoryPriorities[o2.category] ?: AVERAGE
    return o1Priority.compareTo(o2Priority)
  }
}


object FocusBiasPriorityComparator: PriorityComparator {
  override fun compare(colonist: Colonist, o1: Task<*>, o2: Task<*>): Int {
    val assignedToO1 = o1.assigned.contains(colonist.id)
    val assignedToO2 = o2.assigned.contains(colonist.id)
    return when {
      assignedToO1 && assignedToO2 -> 0
      assignedToO1 -> 1
      assignedToO2 -> -1
      else -> 0
    }
  }
}

object TaskPriorityComparator: PriorityComparator {
  override fun compare(colonist: Colonist, o1: Task<*>, o2: Task<*>) =
      o1.priority.compareTo(o2.priority)
}

object NoOpPriorityComparator: PriorityComparator {
  override fun compare(colonist: Colonist, o1: Task<*>, o2: Task<*>) = 0
}