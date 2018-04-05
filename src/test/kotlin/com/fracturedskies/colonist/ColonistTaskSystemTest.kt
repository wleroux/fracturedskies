package com.fracturedskies.colonist

import com.fracturedskies.colonist.Category.*
import com.fracturedskies.colonist.Priority.*
import com.fracturedskies.engine.Id
import org.junit.Test
import kotlin.test.*


class ColonistTaskSystemTest {

  @Test
  fun itPrioritizesTasks_TaskPriority() {
    val tasks = listOf(
        Task(Id(), CONSTRUCTION, AVERAGE, AllCondition, emptyList(), Unit),
        Task(Id(), CONSTRUCTION, HIGHEST, AllCondition, emptyList(), Unit),
        Task(Id(), CONSTRUCTION, AVERAGE, AllCondition, emptyList(), Unit)
    )
    val colonist = Colonist(Id(), mapOf(), AllCondition, TaskPriorityComparator)
    val desiredTask = getDesiredTask(colonist, tasks)
    assertEquals(HIGHEST, desiredTask!!.priority)
  }

  @Test
  fun itPrioritizesTasks_CategoryPriority() {
    val tasks = listOf(
        Task(Id(), CONSTRUCTION, Priority.AVERAGE, AllCondition, emptyList(), Unit),
        Task(Id(), MINE, Priority.AVERAGE, AllCondition, emptyList(), Unit)
    )
    val colonist = Colonist(Id(), mapOf(CONSTRUCTION to 10, MINE to 9), AllCondition, CategoryPriorityComparator)
    val desiredTask = getDesiredTask(colonist, tasks)
    assertEquals(CONSTRUCTION, desiredTask?.category)
  }

  @Test
  fun itFilters_TaskConditions() {
    val tasks = listOf(Task(Id(), MINE, AVERAGE, NoneCondition, emptyList(), Unit))
    val colonist = Colonist(Id(), mapOf(), AllCondition, NoOpPriorityComparator)

    assertNull(getDesiredTask(colonist, tasks))
  }


  @Test
  fun itFilters_ColonistConditions() {
    val tasks = listOf(Task(Id(), MINE, AVERAGE, AllCondition, emptyList(), Unit))
    val colonist = Colonist(Id(), mapOf(), NoneCondition, NoOpPriorityComparator)

    assertNull(getDesiredTask(colonist, tasks))
  }
}