package com.fracturedskies.task.api

import com.fracturedskies.engine.Id


data class Colonist(
    val id: Id,
    val categoryPriorities: Map<TaskCategory, Int>,
    val taskCondition: Condition,
    val taskPriorityComparator: PriorityComparator
)
