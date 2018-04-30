package com.fracturedskies.api.zone

import com.fracturedskies.engine.Id
import com.fracturedskies.engine.math.Vector3i


class Zone(
    val id: Id,
    val positions: Collection<Vector3i>
)