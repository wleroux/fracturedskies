package com.fracturedskies.engine

import com.fracturedskies.engine.events.Event
import com.fracturedskies.engine.events.Cause
import com.fracturedskies.engine.events.Context

data class Initialize(override val cause: Cause, override val context: Context): Event
data class Update(val dt: Long, override val cause: Cause, override val context: Context): Event
data class Render(val alpha: Float, override val cause: Cause, override val context: Context): Event
data class RequestShutdown(override val cause: Cause, override val context: Context): Event
data class Shutdown(override val cause: Cause, override val context: Context): Event
