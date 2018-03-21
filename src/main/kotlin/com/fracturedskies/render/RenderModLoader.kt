package com.fracturedskies.render

import com.fracturedskies.UI_CONTEXT
import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext


class RenderModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(RenderGameSystem(initialContext + UI_CONTEXT).channel)
  }
}