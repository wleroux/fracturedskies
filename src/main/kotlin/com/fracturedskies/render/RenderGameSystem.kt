package com.fracturedskies.render

import com.fracturedskies.engine.*
import com.fracturedskies.engine.events.Cause
import com.fracturedskies.engine.events.Context
import com.fracturedskies.engine.events.Event
import com.fracturedskies.engine.events.EventBus.publish
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.mesh.standard.StandardShaderProgram.*
import kotlinx.coroutines.experimental.launch
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.coroutines.experimental.CoroutineContext

class RenderGameSystem(coroutineContext: CoroutineContext): GameSystem(coroutineContext) {
  suspend override fun invoke(event: Event) {
    when (event) {
      is Initialize -> initialize()
      is Update -> update()
      is Render -> render()
      is Shutdown -> shutdown()
    }
  }

  private var window: Long = 0

  private var block: Block? = null

  private fun initialize() {
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

    // Initialize Resources
    block = Block()
  }

  private suspend fun update() {
    glfwPollEvents()
  }

  private val model: Matrix4 = Matrix4(
    1f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f,
    0f, 0f, 1f, 0f,
    0f, 0f, 0f, 1f
  )

  private val view: Matrix4 = Matrix4(
    1f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f,
    0f, 0f, 1f, 0f,
    0f, 0f, 0f, 1f
  ).invert()

  private val projection: Matrix4 = Matrix4(
    1f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f,
    0f, 0f, 1f, 0f,
    0f, 0f, 0f, 1f
  )

  private fun render() {
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

    // Render Block
    block!!.render(Variables(model, view, projection))

    glfwSwapBuffers(window) // swap the color buffers
  }

  private fun shutdown() {
    glfwFreeCallbacks(window)
    glfwDestroyWindow(window)

    glfwTerminate()
    glfwSetErrorCallback(null).free()
  }

  @Suppress("UNUSED_PARAMETER") private fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) = Unit
  @Suppress("UNUSED_PARAMETER") private fun charModsCallback(window: Long, codepoint: Int, mods: Int) = Unit
  @Suppress("UNUSED_PARAMETER") private fun cursorPosCallback(window: Long, xpos: Double, ypos: Double) = Unit
  @Suppress("UNUSED_PARAMETER") private fun mouseButtonCallback(window: Long, button: Int, action: Int, mods: Int) = Unit
  @Suppress("UNUSED_PARAMETER") private fun windowSizeCallback(window: Long, width: Int, height: Int) = Unit
  @Suppress("UNUSED_PARAMETER") private fun windowCloseCallback(window: Long) {
    launch {
      publish(RequestShutdown(Cause.of(this), Context.empty()))
    }
  }
}