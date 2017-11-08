package com.fracturedskies.render.components.world

import com.fracturedskies.engine.Update
import com.fracturedskies.engine.math.Quaternion4
import com.fracturedskies.engine.math.Vector3
import com.fracturedskies.engine.messages.MessageBus
import com.fracturedskies.engine.messages.MessageBus.unregister
import com.fracturedskies.engine.messages.MessageChannel
import org.lwjgl.glfw.GLFW.*
import java.lang.Math.PI

class Controller {
  companion object {
    private val ROTATE_LEFT = Quaternion4(Vector3.AXIS_Y, (-PI / 4f).toFloat())
    private val ROTATE_RIGHT = Quaternion4(Vector3.AXIS_Y, (PI / 4f).toFloat())
    private val SPEED = 100f
  }
  private var channel: MessageChannel = MessageChannel {
    when (it) {
      is Update -> process(it)
    }
  }

  private var keyPressed: MutableSet<Int> = mutableSetOf()
  fun press(key: Int) {
    keyPressed.add(key)
  }
  fun release(key: Int) {
    keyPressed.remove(key)
  }
  fun clear() {
    keyPressed.clear()
  }
  fun register() {
    MessageBus.register(channel)
  }
  fun unregister() {
    unregister(channel)
  }

  val rotation = Quaternion4(Vector3.AXIS_X, (PI / 3f).toFloat())
  val view = Vector3(64f, 300f, -30f)
  private fun process(update: Update) {
    val movementRotation = rotation.copy(x = 0f, z = 0f).normalize()
    val delta = Vector3(0f, 0f, 0f)
    val forward = keyPressed.contains(GLFW_KEY_UP) or keyPressed.contains(GLFW_KEY_W)
    val backward = keyPressed.contains(GLFW_KEY_DOWN) or keyPressed.contains(GLFW_KEY_S)
    if (forward xor backward) {
      if (forward) {
        delta += Vector3.AXIS_Z * movementRotation
      } else if (backward) {
        delta += Vector3.AXIS_NEG_Z * movementRotation
      }
    }
    val left = keyPressed.contains(GLFW_KEY_LEFT) or keyPressed.contains(GLFW_KEY_A)
    val right = keyPressed.contains(GLFW_KEY_RIGHT) or keyPressed.contains(GLFW_KEY_D)
    if (left xor right) {
      if (left) {
        delta += Vector3.AXIS_NEG_X * movementRotation
      } else if (right) {
        delta += Vector3.AXIS_X * movementRotation
      }
    }
    val lower = keyPressed.contains(GLFW_KEY_LEFT_SHIFT) or keyPressed.contains(GLFW_KEY_RIGHT_SHIFT)
    val upper = keyPressed.contains(GLFW_KEY_SPACE)
    if (upper xor lower) {
      if (upper) {
        delta += Vector3.AXIS_Y
      } else if (lower) {
        delta += Vector3.AXIS_NEG_Y
      }
    }
    delta *= update.dt
    delta *= SPEED
    if (delta.magnitude != 0f) {
      view += delta
    }

    val rotateLeft = keyPressed.contains(GLFW_KEY_Q)
    val rotateRight = keyPressed.contains(GLFW_KEY_E)
    if (rotateLeft xor rotateRight) {
      val deltaRotation = Quaternion4(1f, 0f, 0f, 0f)
      if (rotateLeft) {
        deltaRotation *= ROTATE_LEFT
      } else if (rotateRight) {
        deltaRotation *= ROTATE_RIGHT
      }
      deltaRotation *= update.dt
      rotation *= deltaRotation
    }
  }
}