package com.fracturedskies.engine.api

import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.messages.*

data class Initialize(override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class Update(val dt: Float, override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
data class Shutdown(override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message

data class RequestShutdown(override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()): Message
