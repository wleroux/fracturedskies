package com.fracturedskies.task



import com.fracturedskies.api.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.task.api.TaskPriority.AVERAGE
import kotlin.coroutines.experimental.CoroutineContext


class PickItemsSystem(context: CoroutineContext) {
  val channel = MessageChannel(context) { message ->
    when (message) {
      is ItemSpawned -> {
        send(TaskCreated(Id(), TaskPickItem(message.id), AVERAGE, Cause.of(this)))
      }
    }
  }
}
