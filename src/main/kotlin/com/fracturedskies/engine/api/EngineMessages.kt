package com.fracturedskies.engine.api

import com.fracturedskies.engine.collections.MultiTypeMap

class Initialize(override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
class Update(override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
class Shutdown(override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message
class ShutdownRequested(override val cause: Cause, override val context: MultiTypeMap = MultiTypeMap()) : Message