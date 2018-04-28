package com.fracturedskies.render.world.controller

import com.fracturedskies.Block
import com.fracturedskies.api.*
import com.fracturedskies.api.GameSpeed.*
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.event.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.render.GameState
import com.fracturedskies.render.common.controller.Keyboard
import com.fracturedskies.render.common.events.*
import com.fracturedskies.render.world.components.WorldRenderer.Companion.world
import kotlinx.coroutines.experimental.*
import org.lwjgl.glfw.GLFW.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.math.roundToInt

data class WorldControllerState(
    val zoomLevel: Float = 30f,
    val slice: Int = 0,
    val viewAngle: Float = (Math.PI / 3f).toFloat(),
    val rotationAngle: Float = 0f,
    val viewCenter: Vector3 = Vector3(0f, 0f, 0f),
    val area: Pair<Vector3i, Vector3i>? = null,
    val areaColor: Color4 = Color4(255, 255, 255, 48)
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

    private fun screenPos(bounds: Bounds, mousePos: Point): Pair<Float, Float> {
      val mx = mousePos.x.toFloat() - bounds.x.toFloat()
      val my = mousePos.y.toFloat() - bounds.y.toFloat()
      val sw = bounds.width.toFloat()
      val sh = bounds.height.toFloat()
      val sx = -((sw - mx) / sw - 0.5f) * 2f
      val sy = (my / sh - 0.5f) * 2f
      return sx to sy
    }

    private fun heightAt(world: Space<Block>, x: Int, z: Int, yRange: IntRange): Int {
      val clampedX = clamp(x, 0 until world.dimension.width)
      val clampedZ = clamp(z, 0 until world.dimension.depth)
      return yRange
          .reversed()
          .firstOrNull { world[clampedX, it, clampedZ].type != BlockAir }
          ?: 0
    }
  }

  private val gameState get() = props[GAME_STATE]
  private val world get() = gameState.world!!

  private var focused = false
  private val sliceHeight: Int
    get() = world.blocks.dimension.height - clamp(slice, 0 until world.blocks.dimension.height)

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

  private fun onUpdate(dt: Float) {
    when {
      // Game Speed
      keyboard.isPressed(GLFW_KEY_SPACE) -> send(GameSpeedUpdated(PAUSE, Cause.of(this)))
      keyboard.isPressed(GLFW_KEY_1) -> send(GameSpeedUpdated(SLOW, Cause.of(this)))
      keyboard.isPressed(GLFW_KEY_2) -> send(GameSpeedUpdated(NORMAL, Cause.of(this)))
      keyboard.isPressed(GLFW_KEY_3) -> send(GameSpeedUpdated(FAST, Cause.of(this)))
      keyboard.isPressed(GLFW_KEY_4) -> send(GameSpeedUpdated(UNLIMITED, Cause.of(this)))

      // World Action Controller
      keyboard.isPressed(GLFW_KEY_C) -> worldActionController = SpawnColonistActionController
      keyboard.isPressed(GLFW_KEY_X) -> worldActionController = AddBlockActionController(BlockDirt)
      keyboard.isPressed(GLFW_KEY_B) -> worldActionController = AddBlockActionController(BlockLight)
      keyboard.isPressed(GLFW_KEY_Z) -> worldActionController = RemoveBlockBlockActionController
      keyboard.isPressed(GLFW_KEY_V) -> worldActionController = AddWaterBlockActionController
      keyboard.isPressed(GLFW_KEY_N) -> worldActionController = AddZoneActionController
    }
    worldActionController.onUpdate(world, dt)
    val newArea = worldActionController.area(world)
    if (newArea != state.area) {
      nextState = currentState.copy(
          area = worldActionController.area(world),
          areaColor = worldActionController.areaColor(world)
      )
    }

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

    viewCenter.y = run {
      val yRange = (0 until sliceHeight)
      val viewHeight = heightAt(world.blocks, view.x.toInt(), view.z.toInt(), yRange).toFloat()
      val viewCenterHeight = heightAt(world.blocks, viewCenter.x.toInt(), viewCenter.z.toInt(), yRange).toFloat()
      val minimumHeight = viewHeight + 5f
      val desiredHeight = viewCenterHeight + viewOffset.y

      Math.max(minimumHeight, desiredHeight) - viewOffset.y
    }
  }

  override val handler = EventHandlers(
      on(Key::class, handler = this::onKey),
      on(Focus::class, handler = this::onFocus),
      on(Unfocus::class, handler = this::onUnfocus),
      on(Scroll::class, handler = this::onScroll),
      on(Click::class, handler = this::onClick),
      on(MouseMove::class, handler = this::onMouseMove)
  )


  private fun onScroll(event: Scroll) {
    val yOffset = event.yOffset
    if (keyboard.isPressed(GLFW_KEY_LEFT_CONTROL)) {
      slice += clamp(yOffset.roundToInt(), -1..1)
    } else {
      zoomLevel = clamp(zoomLevel + 5f * yOffset.toFloat(), 5f..100f)
    }
  }

  private var worldActionController: WorldActionController = NoopActionController
  private fun onClick(event: Click) {
    if (!focused)
      return
    if (event.action != GLFW_PRESS && event.action != GLFW_RELEASE)
      return
    event.stopPropogation = true

    val (worldPos, worldDirection) = worldPosDirection(event.mousePos)
    worldActionController.onClick(WorldMouseClick(world, worldPos, worldDirection, event.action, event.button, event.mods), sliceHeight)
  }

  private fun onMouseMove(event: MouseMove) {
    val (worldPos, worldDirection) = worldPosDirection(event.mousePos)
    worldActionController.onMove(WorldMouseMove(world, worldPos, worldDirection), sliceHeight)
  }

  private fun onKey(event: Key) {
    if (event.action == GLFW_PRESS) {
      keyboard = keyboard.press(event.key)
    } else if (event.action == GLFW_RELEASE){
      keyboard = keyboard.release(event.key)
    }
  }

  @Suppress("UNUSED_PARAMETER")
  private fun onFocus(event: Focus) {
    focused = true
  }

  @Suppress("UNUSED_PARAMETER")
  private fun onUnfocus(event: Unfocus) {
    focused = false
    keyboard = Keyboard()
  }

  private fun worldPosDirection(mousePos: Point): Pair<Vector3, Vector3> {
    val (sx, sy) = screenPos(this.bounds, mousePos)

    val perspectiveInverse = Matrix4.perspective(Math.PI.toFloat() / 4, this.bounds.width, this.bounds.height, 0.03f, 1000f).invert()
    val viewInverse = Matrix4(view, rotation)

    var rayStart4 = Vector4(sx, sy, -1f, 1f) * perspectiveInverse * viewInverse
    rayStart4 *= (1f / rayStart4.w)
    val rayStart3 = Vector3(rayStart4)

    var rayEnd4 = Vector4(sx, sy, 1f, 1f) * perspectiveInverse * viewInverse
    rayEnd4 *= (1f / rayEnd4.w)
    val rayEnd3 = Vector3(rayEnd4)
    val direction = (rayEnd3 - rayStart3).normalize()
    return rayStart3 to direction
  }

  override fun render() = nodes {
    world(
      world,
      Matrix4(position = view, rotation = rotation).invert(),
      state.area,
      state.areaColor,
      sliceHeight
    )
  }
}