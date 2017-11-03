package com.fracturedskies.render

import com.fracturedskies.engine.*
import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.Component.Companion.BOUNDS
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.Message
import com.fracturedskies.engine.messages.MessageBus.publish
import com.fracturedskies.render.components.Scene
import com.fracturedskies.render.events.*
import kotlinx.coroutines.experimental.runBlocking
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.coroutines.experimental.CoroutineContext

class RenderGameSystem(coroutineContext: CoroutineContext): GameSystem(coroutineContext) {
  suspend override fun invoke(message: Message) {
    when (message) {
      is Initialize -> initialize()
      is Update -> update()
      is Render -> render()
      is Shutdown -> shutdown()
    }
  }

  private lateinit var rootNode: Node
  private var rootComponent: Component? = null

  private var window: Long = 0

  private suspend fun initialize() {
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
    val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
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

    glfwSetInputMode(window, GLFW_STICKY_KEYS, 1)
    glfwSetWindowPos(window, (videoMode.width() - screenWidth) / 2, (videoMode.height() - screenHeight) / 2)
    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)
    GL.createCapabilities()

    glfwShowWindow(window)

    // Initialize GL
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glEnable(GL_CULL_FACE)
    glCullFace(GL_FRONT)

    rootNode = Node(::Scene, Context(BOUNDS to Bounds(0, 0, screenWidth, screenHeight)))
  }

  private suspend fun update() {
    glfwPollEvents()
  }

  private suspend fun render() {
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

    // Render Scene
    rootComponent = rootNode.toComponent(rootComponent)
    rootComponent?.render()

    glfwSwapBuffers(window) // swap the color buffers
  }

  private suspend fun shutdown() {
    rootComponent?.unmount()

    glfwFreeCallbacks(window)
    glfwDestroyWindow(window)

    glfwTerminate()
    glfwSetErrorCallback(null).free()
  }

  private var focus: Component? = null
  @Suppress("UNUSED_PARAMETER") private fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
    if (focus != null) {
      focus?.dispatch(Key(focus!!, key, scancode, action, mods))
    }
  }
  @Suppress("UNUSED_PARAMETER") private fun charModsCallback(window: Long, codepoint: Int, mods: Int) {
    if (focus != null) {
      focus?.dispatch(CharMods(focus!!, codepoint, mods))
    }
  }

  private var mousePos = Point(0, 0)
  private var hover: Component? = null
  @Suppress("UNUSED_PARAMETER") private fun cursorPosCallback(window: Long, xpos: Double, ypos: Double) {
    mousePos = Point(xpos.toInt(), ypos.toInt())
    val nextHover = rootComponent?.componentFromPoint(mousePos)
    val prevHover = hover
    if (prevHover !== nextHover) {
      prevHover?.dispatch(Unhover(prevHover))
      nextHover?.dispatch(Hover(nextHover))
      hover = nextHover
    }
  }
  @Suppress("UNUSED_PARAMETER") private fun mouseButtonCallback(window: Long, button: Int, action: Int, mods: Int) {
    val component = rootComponent?.componentFromPoint(mousePos)
    if (component != null) {
      val event = Click(component, mousePos, button, action, mods)
      component.dispatch(event)
      if (!event.stopPropogation) {
        val prevFocus = focus
        val nextFocus = component

        if (prevFocus !== nextFocus) {
          prevFocus?.dispatch(Unfocus(prevFocus))
          nextFocus.dispatch(Focus(nextFocus))
          focus = nextFocus
        }
      }
    }
  }

  @Suppress("UNUSED_PARAMETER") private fun windowSizeCallback(window: Long, width: Int, height: Int) {
    rootNode = Node(::Scene, Context(BOUNDS to Bounds(0, 0, width, height)))
  }
  @Suppress("UNUSED_PARAMETER") private fun windowCloseCallback(window: Long) = runBlocking {
    publish(RequestShutdown(Cause.of(this@RenderGameSystem), Context()))
  }
}