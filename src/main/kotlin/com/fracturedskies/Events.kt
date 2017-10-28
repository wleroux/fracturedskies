package com.fracturedskies

import com.fracturedskies.events.Cancellable
import com.fracturedskies.events.Event
import com.fracturedskies.events.Cause
import com.fracturedskies.events.Context

data class NewGameRequested(val name: String, override val cause: Cause, override val context: Context): Event, Cancellable {
  override var cancelled = false
}