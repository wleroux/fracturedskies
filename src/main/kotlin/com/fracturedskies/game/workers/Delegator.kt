package com.fracturedskies.game.workers

import com.fracturedskies.engine.GameSystem
import com.fracturedskies.engine.Update
import com.fracturedskies.engine.messages.Message
import com.fracturedskies.game.messages.QueueWork
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

class Delegator(coroutineContext: CoroutineContext = EmptyCoroutineContext) : GameSystem(coroutineContext) {
  private val globalWork = mutableListOf<Work>()
  private val workers = mutableListOf<Worker>()

  init {
    workers.add(Worker())
  }

  suspend override fun invoke(message: Message) {
    when(message) {
      is QueueWork -> {
        globalWork.add(message.work)
      }
      is Update -> {
        workers
                .filter { !it.isBusy() }
                .forEach {
                  val availableWork = globalWork + it.personalWork
                  if (availableWork.isNotEmpty()) {
                    val work = it.prioritize(availableWork).first()
                    it.receive(work)

                    globalWork.remove(work)
                    it.personalWork.remove(work)
                  }
                }
      }
    }
  }
}