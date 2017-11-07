package com.fracturedskies.game.messages

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.Message
import com.fracturedskies.game.World

data class WorldGenerated(val world: World, override val cause: Cause, override val context: Context): Message
data class NewGameRequested(override val cause: Cause, override val context: Context): Message
