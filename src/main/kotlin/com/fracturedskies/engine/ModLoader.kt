package com.fracturedskies.engine

import kotlin.coroutines.experimental.CoroutineContext


abstract class ModLoader {
  open fun initialize(initialContext: CoroutineContext) = Unit
  open fun start() = Unit
}