package com.fracturedskies.task.api

import com.fracturedskies.engine.Id
import com.fracturedskies.task.api.TaskPriority.AVERAGE



data class Task<out T>(
    val id: Id,
    val category: TaskCategory,
    val priority: TaskPriority = AVERAGE,
    val condition: Condition,
    val assigned: List<Id>,
    val taskDetails: T
)
