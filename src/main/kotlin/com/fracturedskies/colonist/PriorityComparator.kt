package com.fracturedskies.colonist

interface PriorityComparator {
  fun compare(colonist: Colonist, o1: Task<*>, o2: Task<*>): Int
}

val ColonistPriorityComparator = CompoundPriorityComparator(
    FocusBiasPriorityComparator,
    CategoryPriorityComparator,
    TaskPriorityComparator
)

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
    val o1Priority = colonist.categoryPriorities[o1.category]
    val o2Priority = colonist.categoryPriorities[o2.category]
    return if (o1Priority == null && o2Priority != null) {
      1
    } else if (o1Priority != null && o2Priority == null) {
      -1
    } else if (o1Priority == null && o2Priority == null) {
      0
    } else {
      o1Priority!!.compareTo(o2Priority!!)
    }
  }
}

object NoOpPriorityComparator: PriorityComparator {
  override fun compare(colonist: Colonist, o1: Task<*>, o2: Task<*>) = 0
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