package com.fracturedskies.engine

import java.util.*

data class Id(private val uuid: UUID) {
  companion object {
    operator fun invoke(): Id {
      return Id(UUID.randomUUID())
    }
  }

  override fun toString() = "Id($uuid)"
}
