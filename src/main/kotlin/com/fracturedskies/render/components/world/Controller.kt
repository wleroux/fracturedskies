package com.fracturedskies.render.components.world

import com.fracturedskies.engine.Update
import com.fracturedskies.engine.math.Quaternion4
import com.fracturedskies.engine.math.Vector3
import com.fracturedskies.engine.math.clamp
import com.fracturedskies.engine.messages.MessageBus
import com.fracturedskies.engine.messages.MessageBus.unregister
import com.fracturedskies.engine.messages.MessageChannel
import org.lwjgl.glfw.GLFW.*
import java.lang.Math.PI
import kotlin.math.roundToInt

class Controller {
  companion object {
    private val ROTATE_LEFT = (PI / 32f).toFloat()
    private val ROTATE_RIGHT = (-PI / 32f).toFloat()
    private val ROTATE_UP = (-PI / 4f).toFloat()
    private val ROTATE_DOWN = (PI / 4f).toFloat()
    private val SPEED = 1f
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

  private var zoomLevel: Float = 30f
  var slice: Int = 0

  @Suppress("UNUSED_PARAMETER") fun scroll(xOffset: Double, yOffset: Double) {
    if (keyPressed.contains(GLFW_KEY_LEFT_CONTROL)) {
      slice += clamp(yOffset.roundToInt(), -1..1)
    } else {
      zoomLevel = clamp(zoomLevel + 5f * yOffset.toFloat(), 5f..100f)
    }
  }
  fun register() {
    MessageBus.register(channel)
  }
  fun unregister() {
    unregister(channel)
  }

  private var viewAngle = (PI / 3f).toFloat()
  private var rotationAngle = 0f
  val rotation get() = Quaternion4(Vector3.AXIS_X, viewAngle) * Quaternion4(Vector3.AXIS_Y,  rotationAngle)
  val view get() = viewCenter + viewOffset
  var viewCenter = Vector3(64f, 0f, 0f)
  val viewOffset get() = Vector3(0f, 0f, -1f).times(rotation).times(zoomLevel)
  private fun process(update: Update) {
    // Perspective
    val lookUp = (keyPressed.contains(GLFW_KEY_UP) or keyPressed.contains(GLFW_KEY_W)) and keyPressed.contains(GLFW_KEY_LEFT_SHIFT)
    val lookDown = (keyPressed.contains(GLFW_KEY_DOWN) or keyPressed.contains(GLFW_KEY_S)) and keyPressed.contains(GLFW_KEY_LEFT_SHIFT)
    viewAngle = clamp(when {
      lookUp and !lookDown -> viewAngle + update.dt * SPEED * ROTATE_UP
      lookDown and !lookUp -> viewAngle + update.dt * SPEED * ROTATE_DOWN
      else -> viewAngle
    }, 0f..(PI / 2f).toFloat())

    val rotateLeft = keyPressed.contains(GLFW_KEY_Q)
    val rotateRight = keyPressed.contains(GLFW_KEY_E)
    rotationAngle = when {
      rotateLeft && !rotateRight -> rotationAngle + SPEED * ROTATE_LEFT
      rotateRight && !rotateLeft -> rotationAngle + SPEED * ROTATE_RIGHT
      else -> rotationAngle
    }

    // Movement
    val movementRotation = rotation.copy(x = 0f, z = 0f).normalize()
    var delta = Vector3(0f, 0f, 0f)
    val forward = (keyPressed.contains(GLFW_KEY_UP) or keyPressed.contains(GLFW_KEY_W)) and !keyPressed.contains(GLFW_KEY_LEFT_SHIFT)
    val backward = (keyPressed.contains(GLFW_KEY_DOWN) or keyPressed.contains(GLFW_KEY_S)) and !keyPressed.contains(GLFW_KEY_LEFT_SHIFT)
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
    delta *= SPEED * zoomLevel
    if (delta.magnitude != 0f) {
      viewCenter += delta
    }
  }
}