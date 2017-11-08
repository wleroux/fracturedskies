package com.fracturedskies.engine

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.Message

data class Initialize(override val cause: Cause, override val context: Context): Message
data class Update(val dt: Float, override val cause: Cause, override val context: Context): Message
data class Render(val alpha: Float, override val cause: Cause, override val context: Context): Message
data class RequestShutdown(override val cause: Cause, override val context: Context): Message
data class Shutdown(override val cause: Cause, override val context: Context): Message
