package com.fracturedskies.render.components.world

import com.fracturedskies.api.*
import com.fracturedskies.colonist.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.event.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.render.GameState
import com.fracturedskies.render.components.world.WorldRenderer.Companion.worldRenderer
import com.fracturedskies.render.controller.Keyboard
import com.fracturedskies.render.events.*
import com.fracturedskies.water.api.MAX_WATER_LEVEL
import kotlinx.coroutines.experimental.*
import org.lwjgl.glfw.GLFW.*
import java.lang.Integer.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.math.roundToInt

data class WorldControllerState(
    val zoomLevel: Float = 30f,
    val slice: Int = 0,
    val viewAngle: Float = (Math.PI / 3f).toFloat(),
    val rotationAngle: Float = 0f,
    val viewCenter: Vector3 = Vector3(0f, 0f, 0f)
)
class WorldController(props: MultiTypeMap) : Component<WorldControllerState>(props, WorldControllerState()) {

  companion object {
    fun Node.Builder<*>.worldController(gameState: GameState, additionalContext: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::WorldController, additionalContext.with(
          GAME_STATE to gameState)))
    }

    val GAME_STATE = TypedKey<GameState>("wcGameState")

    private const val ROTATE_LEFT = (Math.PI / 32f).toFloat()
    private const val ROTATE_RIGHT = (-Math.PI / 32f).toFloat()
    private const val ROTATE_UP = (-Math.PI / 4f).toFloat()
    private const val ROTATE_DOWN = (Math.PI / 4f).toFloat()
    private const val SPEED = 1f

    private fun heightAt(world: Space<Block>, x: Int, z: Int, yRange: IntRange): Int {
      val clampedX = clamp(x, 0 until world.dimension.width)
      val clampedZ = clamp(z, 0 until world.dimension.depth)
      return yRange
          .reversed()
          .firstOrNull { world[clampedX, it, clampedZ].type != BlockType.AIR }
          ?: 0
    }
  }

  private val gameState get() = requireNotNull(props[GAME_STATE])
  private val world get() = requireNotNull(gameState.world)
  private val initialized get() = gameState.initialized
  private val workers get() = requireNotNull(gameState.workers)
  private val timeOfDay get() = gameState.timeOfDay

  private var firstBlock: Vector3i? = null
  private var focused = false
  private val sliceHeight: Int
    get() = world.dimension.height - clamp(slice, 0 until world.dimension.height)

  private var keyboard = Keyboard()
  private var zoomLevel
    get() = (nextState ?: state).zoomLevel
    set(zoomLevel) {
      nextState = (nextState ?: state).copy(zoomLevel = zoomLevel)
    }
  private var slice
    get() = (nextState ?: state).slice
    set(slice) {
      nextState = (nextState ?: state).copy(slice = slice)
    }
  private var viewAngle
    get() = (nextState ?: state).viewAngle
    set(viewAngle) {
      nextState = (nextState ?: state).copy(viewAngle = viewAngle)
    }
  private var rotationAngle
    get() = (nextState ?: state).rotationAngle
    set(rotationAngle) {
      nextState = (nextState ?: state).copy(rotationAngle = rotationAngle)
    }
  private var viewCenter
    get() = (nextState ?: state).viewCenter
    set(viewCenter) {
      nextState = (nextState ?: state).copy(viewCenter = viewCenter)
    }

  private val rotation get() = Quaternion4(Vector3.AXIS_X, viewAngle) * Quaternion4(Vector3.AXIS_Y,  rotationAngle)
  private val view get() = viewCenter + viewOffset
  private val viewOffset get() = Vector3(0f, 0f, -1f).times(rotation).times(zoomLevel)

  @Suppress("UNUSED_PARAMETER")
  private fun scroll(xOffset: Double, yOffset: Double) {
    if (keyboard.isPressed(GLFW_KEY_LEFT_CONTROL)) {
      slice += clamp(yOffset.roundToInt(), -1..1)
    } else {
      zoomLevel = clamp(zoomLevel + 5f * yOffset.toFloat(), 5f..100f)
    }
  }

  private fun onUpdate(dt: Float) {
    // Perspective
    val lookUp = (keyboard.isPressed(GLFW_KEY_UP) or keyboard.isPressed(GLFW_KEY_W)) and keyboard.isPressed(GLFW_KEY_LEFT_SHIFT)
    val lookDown = (keyboard.isPressed(GLFW_KEY_DOWN) or keyboard.isPressed(GLFW_KEY_S)) and keyboard.isPressed(GLFW_KEY_LEFT_SHIFT)
    viewAngle = clamp(when {
      lookUp and !lookDown -> viewAngle + dt * SPEED * ROTATE_UP
      lookDown and !lookUp -> viewAngle + dt * SPEED * ROTATE_DOWN
      else -> viewAngle
    }, 0f..(Math.PI / 2f).toFloat())

    val rotateLeft = keyboard.isPressed(GLFW_KEY_Q)
    val rotateRight = keyboard.isPressed(GLFW_KEY_E)
    rotationAngle = when {
      rotateLeft && !rotateRight -> rotationAngle + SPEED * ROTATE_LEFT
      rotateRight && !rotateLeft -> rotationAngle + SPEED * ROTATE_RIGHT
      else -> rotationAngle
    }

    // Movement
    val movementRotation = rotation.copy(x = 0f, z = 0f).normalize()
    var delta = Vector3(0f, 0f, 0f)
    val forward = (keyboard.isPressed(GLFW_KEY_UP) or keyboard.isPressed(GLFW_KEY_W)) and !keyboard.isPressed(GLFW_KEY_LEFT_SHIFT)
    val backward = (keyboard.isPressed(GLFW_KEY_DOWN) or keyboard.isPressed(GLFW_KEY_S)) and !keyboard.isPressed(GLFW_KEY_LEFT_SHIFT)
    if (forward xor backward) {
      if (forward) {
        delta += Vector3.AXIS_Z * movementRotation
      } else if (backward) {
        delta += Vector3.AXIS_NEG_Z * movementRotation
      }
    }
    val left = keyboard.isPressed(GLFW_KEY_LEFT) or keyboard.isPressed(GLFW_KEY_A)
    val right = keyboard.isPressed(GLFW_KEY_RIGHT) or keyboard.isPressed(GLFW_KEY_D)
    if (left xor right) {
      if (left) {
        delta += Vector3.AXIS_NEG_X * movementRotation
      } else if (right) {
        delta += Vector3.AXIS_X * movementRotation
      }
    }
    delta *= dt
    delta *= SPEED * zoomLevel
    if (delta.magnitude != 0f) {
      viewCenter += delta
    }
  }

  private lateinit var controllerUpdates: Job
  override fun componentWillMount() {
    super.componentWillMount()
    controllerUpdates = launch {
      while (isActive) {
        onUpdate(16f/1000f)
        delay(16, MILLISECONDS)
      }
    }
  }

  override fun componentWillUnmount() {
    super.componentWillUnmount()
    controllerUpdates.cancel()
  }

  override fun componentWillUpdate(nextProps: MultiTypeMap, nextState: WorldControllerState) {
    super.componentWillUpdate(nextProps, nextState)

    if (initialized) {
      viewCenter.y = run {
        val yRange = (0 until sliceHeight)
        val viewHeight = heightAt(world, view.x.toInt(), view.z.toInt(), yRange).toFloat()
        val viewCenterHeight = heightAt(world, viewCenter.x.toInt(), viewCenter.z.toInt(), yRange).toFloat()
        val minimumHeight = viewHeight + 5f
        val desiredHeight = viewCenterHeight + viewOffset.y

        Math.max(minimumHeight, desiredHeight) - viewOffset.y
      }
    }
  }

  override val handler = EventHandlers(on(Key::class) { key ->
    if (key.action == GLFW_PRESS) {
      keyboard = keyboard.press(key.key)
    } else if (key.action == GLFW_RELEASE){
      keyboard = keyboard.release(key.key)
    }
  }, on(Focus::class) {
    focused = true
  }, on(Unfocus::class) {
    focused = false
    keyboard = Keyboard()
  }, on(Scroll::class) { event ->
    scroll(event.xOffset, event.yOffset)
  },on(Click::class) { event->
    if (!focused) {
      return@on
    }
    if (event.action != GLFW_PRESS && event.action != GLFW_RELEASE) {
      return@on
    }
    event.stopPropogation = true

    val mx = event.mousePos.x.toFloat() - this.bounds.x.toFloat()
    val my = event.mousePos.y.toFloat() - this.bounds.y.toFloat()
    val sw = this.bounds.width.toFloat()
    val sh = this.bounds.height.toFloat()
    val sx = -((sw - mx) / sw - 0.5f) * 2
    val sy = (my / sh - 0.5f) * 2

    val perspectiveInverse = Matrix4.perspective(Math.PI.toFloat() / 4, this.bounds.width, this.bounds.height, 0.03f, 1000f).invert()
    val viewInverse = Matrix4(view, rotation)

    var rayStart4 = Vector4(sx, sy, -1f, 1f) * perspectiveInverse * viewInverse
    rayStart4 *= (1f / rayStart4.w)
    val rayStart3 = Vector3(rayStart4) - Vector3.ZERO

    var rayEnd4 = Vector4(sx, sy, 1f, 1f) * perspectiveInverse * viewInverse
    rayEnd4 *= (1f / rayEnd4.w)
    val rayEnd3 = Vector3(rayEnd4) - Vector3.ZERO
    val direction = (rayEnd3 - rayStart3).normalize()

    val selectedBlock = raycast(world, rayStart3, direction)
            .filter { it.position.y < sliceHeight }
            .filterNot { it.obj.type == BlockType.AIR }
            .firstOrNull()
    if (selectedBlock == null) {
      firstBlock = null
    } else {
      when (event.action) {
        GLFW_PRESS -> {
          // Add Blocks or Add Water
          if (event.button == GLFW_MOUSE_BUTTON_LEFT || event.button == GLFW_MOUSE_BUTTON_MIDDLE) {
            firstBlock = selectedBlock.position + selectedBlock.faces.first()
          }
          // Add Water or Remove Blocks
          else if (event.button == GLFW_MOUSE_BUTTON_RIGHT) {
            firstBlock = selectedBlock.position
          }
        }
        GLFW_RELEASE -> {
          if (firstBlock != null) {
            // Add Blocks
            var secondBlock: Vector3i? = null
            if (event.button == GLFW_MOUSE_BUTTON_LEFT || event.button == GLFW_MOUSE_BUTTON_MIDDLE) {
              secondBlock = selectedBlock.position + selectedBlock.faces.first()
            }
            // Add Water or Remove Blocks
            else if (event.button == GLFW_MOUSE_BUTTON_RIGHT) {
              secondBlock = selectedBlock.position
            }

            val xRange = min(firstBlock!!.x, secondBlock!!.x)..max(firstBlock!!.x, secondBlock.x)
            val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
            val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)

            // Add Blocks
            if (event.button == GLFW_MOUSE_BUTTON_LEFT) {
              if (!keyboard.isPressed(GLFW_KEY_LEFT_CONTROL)) {
                val blockType = when {
                  keyboard.isPressed(GLFW_KEY_LEFT_SHIFT) -> BlockType.LIGHT
                  else -> BlockType.BLOCK
                }

                val updates = xRange.flatMap { x ->
                  zRange.flatMap { z ->
                    yRange.flatMap { y ->
                      if (world.has(x, y, z) && !world[x, y, z].type.opaque)
                        listOf(Vector3i(x, y, z) to blockType)
                      else listOf()
                    }
                  }
                }.toMap()
                updates.forEach { update ->
                  send(TaskCreated(Id(), Category.MINE, Priority.AVERAGE, SingleAssigneeCondition, PlaceBlock(update.key, update.value), Cause.of(this)))
                }
              } else {
                send(ColonistSpawned(Id(), Vector3i(xRange.start, yRange.start, zRange.start), Cause.of(this)))
              }
            }
            // Remove Blocks
            else if (event.button == GLFW_MOUSE_BUTTON_RIGHT) {
              val updates = xRange.flatMap { x ->
                zRange.flatMap { z ->
                  yRange.flatMap { y ->
                    if (world.has(x, y, z) && world[x, y, z].type.opaque)
                      listOf(Vector3i(x, y, z) to BlockType.AIR)
                    else listOf()
                  }
                }
              }.toMap()
              updates.forEach { update ->
                send(TaskCreated(Id(), Category.MINE, Priority.AVERAGE, SingleAssigneeCondition, RemoveBlock(update.key), Cause.of(this)))
              }
            }
            // Add Water
            else if (event.button == GLFW_MOUSE_BUTTON_MIDDLE) {
              val updates = xRange.flatMap { x ->
                zRange.flatMap { z ->
                  yRange.flatMap { y ->
                    if (world.has(x, y, z) && !world[x, y, z].type.opaque) {
                      if (event.button == GLFW_MOUSE_BUTTON_1) {
                        val waterLevel = world[x, y, z].waterLevel
                        if (waterLevel > 0.toByte()) {
                          listOf(Vector3i(x, y, z) to waterLevel.dec())
                        } else listOf()
                      } else {
                        val waterLevel = world[x, y, z].waterLevel
                        if (waterLevel < MAX_WATER_LEVEL) {
                          listOf(Vector3i(x, y, z) to MAX_WATER_LEVEL)
                        } else listOf()
                      }
                    } else listOf()
                  }
                }
              }.toMap()

              send(BlockWaterLevelUpdated(updates, Cause.of(this)))
            }
          }
          firstBlock = null
        }
      }
    }
  })

  override fun render() = nodes {
    if (initialized) {
      worldRenderer(MultiTypeMap(
          WorldRenderer.VIEW to view,
          WorldRenderer.ROTATION to rotation,
          WorldRenderer.SLICE_HEIGHT to sliceHeight,
          WorldRenderer.TIME_OF_DAY to timeOfDay,
          WorldRenderer.WORLD to world,
          WorldRenderer.WORKERS to workers
          ))
    }
  }
}