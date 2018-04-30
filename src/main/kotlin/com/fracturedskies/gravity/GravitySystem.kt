package com.fracturedskies.gravity

import com.fracturedskies.api.World
import com.fracturedskies.engine.api.*
import com.fracturedskies.engine.math.Vector3i
import javax.enterprise.event.*
import javax.inject.*

@Singleton
class GravitySystem {

  @Inject
  lateinit var events: Event<Any>

  @Inject
  lateinit var world: World

  fun onUpdate(@Observes update: Update) {
    world.items
        .filterValues { it.position != null }
        .forEach { id, item ->
      val belowPos = item.position!! - Vector3i.AXIS_Y
      if (world.has(belowPos) && !world.blocks[belowPos].type.opaque) {
        world.moveItem(id, belowPos, Cause.of(this))
      }
    }
  }
}