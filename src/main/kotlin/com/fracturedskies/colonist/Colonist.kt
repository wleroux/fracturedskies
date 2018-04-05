package com.fracturedskies.colonist

import com.fracturedskies.engine.Id


data class Colonist(
    val id: Id,
    val categoryPriorities: Map<Category, Int>,
    val taskCondition: Condition,
    val taskPriorityComparator: PriorityComparator
)
