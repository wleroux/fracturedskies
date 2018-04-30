package com.fracturedskies.api.entity

import com.fracturedskies.engine.Id
import com.fracturedskies.engine.math.Vector3i


class Item(
    val id: Id,
    var position: Vector3i?,
    var colonist: Id?,
    val itemType: ItemType
)
