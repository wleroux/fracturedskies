package com.fracturedskies.game.workers

import com.fracturedskies.engine.Update
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus
import com.fracturedskies.game.Game
import com.fracturedskies.game.messages.WorkAssignedToWorker
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

class Delegator(coroutineContext: CoroutineContext = EmptyCoroutineContext) {
  val game = Game(coroutineContext) { message ->
    when(message) {
      is WorkAssignedToWorker -> {
        message.worker.receive(message.work)
        message.worker.personalWork.remove(message.work)
      }
      is Update -> {
        var availableGlobalWork = globalWork.toList()
        workers
                .filter { !it.isBusy() }
                .forEach {
                  val availableWork = availableGlobalWork + it.personalWork
                  if (availableWork.isNotEmpty()) {
                    val work = it.prioritize(availableWork).first()

                    MessageBus.send(WorkAssignedToWorker(it, work, Cause.of(this), Context()))
                    availableGlobalWork -= work
                  }
                }
      }
    }
  }
  val channel get() = game.channel

  private val workers = mutableListOf<Worker>()
  init {
    (0..20).forEach {
      workers.add(Worker())
    }
  }
}
