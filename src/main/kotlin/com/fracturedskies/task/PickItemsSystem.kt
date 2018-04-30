package com.fracturedskies.task

import com.fracturedskies.api.*
import com.fracturedskies.api.task.TaskPickItem
import com.fracturedskies.api.task.TaskPriority.AVERAGE
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.api.Cause
import javax.enterprise.event.Observes
import javax.inject.*


@Singleton
class PickItemsSystem {

  @Inject
  lateinit var world: World

  fun onItemSpawned(@Observes itemSpawned: ItemSpawned) {
    world.createTask(Id(), TaskPickItem(itemSpawned.itemId), AVERAGE, Cause.of(this))
  }
}
