package com.fracturedskies.treefall

import com.fracturedskies.engine.ModLoader
import com.fracturedskies.engine.messages.MessageBus.register
import kotlin.coroutines.experimental.CoroutineContext


class TreeFallModLoader: ModLoader() {
  override fun initialize(initialContext: CoroutineContext) {
    register(TreeFallSystem(initialContext).channel)
  }
}