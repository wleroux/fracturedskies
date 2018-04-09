package com.fracturedskies.render

import com.fracturedskies.engine.api.*
import com.fracturedskies.engine.collections.MultiTypeMap
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.Component.Companion.dispatch
import com.fracturedskies.engine.jeact.Component.Companion.mount
import com.fracturedskies.engine.jeact.Component.Companion.unmount
import com.fracturedskies.engine.jeact.Component.Companion.update
import com.fracturedskies.engine.messages.*
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.render.components.Scene
import com.fracturedskies.render.events.*
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.coroutines.experimental.CoroutineContext

class RenderGameSystem(context: CoroutineContext) {
  private var nextGameState: GameState = GameState()
  val channel = MessageChannel(context) { message ->
    nextGameState = updateGameState(nextGameState, message)
    if (message is Update)
      gameState = nextGameState
  }

  private var gameState = nextGameState
  private lateinit var scene: Scene
  private lateinit var screenDimension: Bounds

  private var window: Long = 0

  fun glInitialize() {
    GLFWErrorCallback.createPrint(System.err).set()

    // Initialize GLFW
    require(glfwInit())

    // Client API Hints
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5)
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    // Window attributes hints
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    // Create window
    val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!
    val screenWidth = videoMode.width() / 2
    val screenHeight = videoMode.height() / 2
    window = requireNotNull(glfwCreateWindow(screenWidth, screenHeight, "Fractured Skies", NULL, NULL))

    // Register Callbacks
    glfwSetKeyCallback(window, this::keyCallback)
    glfwSetCharModsCallback(window, this::charModsCallback)
    glfwSetCursorPosCallback(window, this::cursorPosCallback)
    glfwSetMouseButtonCallback(window, this::mouseButtonCallback)
    glfwSetWindowSizeCallback(window, this::windowSizeCallback)
    glfwSetWindowCloseCallback(window, this::windowCloseCallback)
    glfwSetScrollCallback(window, this::scrollCallback)

    glfwSetInputMode(window, GLFW_STICKY_KEYS, 1)
    glfwSetWindowPos(window, (videoMode.width() - screenWidth) / 2, (videoMode.height() - screenHeight) / 2)
    glfwMakeContextCurrent(window)
    GL.createCapabilities()

    glfwShowWindow(window)

    // Initialize GL
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glEnable(GL_CULL_FACE)
    glCullFace(GL_FRONT)

    scene = mount(::Scene, null, MultiTypeMap(
        Scene.GAME_STATE to gameState
    ))
    update(scene, MultiTypeMap(
        Scene.GAME_STATE to gameState
    ), true)
  }

  fun glUpdate() {
    glfwPollEvents()
  }

  fun glRender() {
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    update(scene, MultiTypeMap(
        Scene.GAME_STATE to gameState
    ), false)
    scene.glRender(screenDimension)
    glfwSwapBuffers(window)
  }

  fun glShutdown() {
    unmount(scene)

    glfwFreeCallbacks(window)
    glfwDestroyWindow(window)

    glfwTerminate()
    glfwSetErrorCallback(null)!!.free()
  }

  private var focus: Component<*>? = null
  @Suppress("UNUSED_PARAMETER") private fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
    if (focus != null) {
      dispatch(focus!!, Key(focus!!, key, scancode, action, mods))
    }
  }
  @Suppress("UNUSED_PARAMETER") private fun charModsCallback(window: Long, codepoint: Int, mods: Int) {
    if (focus != null) {
      dispatch(focus!!, CharMods(focus!!, codepoint, mods))
    }
  }

  private var mousePos = Point(0, 0)
  private var hover: Component<*>? = null
  @Suppress("UNUSED_PARAMETER") private fun cursorPosCallback(window: Long, xpos: Double, ypos: Double) {
    mousePos = Point(xpos.toInt(), screenDimension.height - ypos.toInt())
    val nextHover = scene.glComponentFromPoint(mousePos)
    val prevHover = hover
    if (prevHover !== nextHover) {
      if (prevHover != null)
        dispatch(prevHover, Unhover(prevHover))
      if (nextHover != null)
        dispatch(nextHover, Hover(nextHover))
      hover = nextHover
    }
  }
  @Suppress("UNUSED_PARAMETER") private fun mouseButtonCallback(window: Long, button: Int, action: Int, mods: Int) {
    val nextFocus = scene.glComponentFromPoint(mousePos)
    if (nextFocus != null) {
      val event = Click(nextFocus, mousePos, button, action, mods)
      dispatch(nextFocus, event)
      if (!event.stopPropogation) {
        val prevFocus = focus
        if (prevFocus !== nextFocus) {
          if (prevFocus != null)
            dispatch(prevFocus, Unfocus(prevFocus))
          dispatch(nextFocus, Focus(nextFocus))
          focus = nextFocus
        }
      }
    }
  }
  @Suppress("UNUSED_PARAMETER") private fun scrollCallback(window: Long, xoffset: Double, yoffset: Double) {
    if (focus != null)
      dispatch(focus!!, Scroll(focus!!, xoffset, yoffset))
  }

  @Suppress("UNUSED_PARAMETER") private fun windowSizeCallback(window: Long, width: Int, height: Int) {
    screenDimension = Bounds(0, 0, width, height)
  }
  @Suppress("UNUSED_PARAMETER") private fun windowCloseCallback(window: Long) {
    send(ShutdownRequested(Cause.of(this@RenderGameSystem), MultiTypeMap()))
  }
}