package com.fracturedskies.render.common.controller


data class Keyboard(private val keysPressed: Set<Int> = emptySet()) {
  fun press(key: Int) = if (isPressed(key)) this else Keyboard(keysPressed.toMutableSet().apply { add(key) })
  fun release(key: Int) = if (!isPressed(key)) this else Keyboard(keysPressed.toMutableSet().apply { remove(key) })
  fun isPressed(key: Int) = keysPressed.contains(key)
}