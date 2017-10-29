package com.fracturedskies.render

import com.fracturedskies.engine.*
import com.fracturedskies.engine.events.Cause
import com.fracturedskies.engine.events.Event
import com.fracturedskies.engine.events.EventBus.publish
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWWindowSizeCallbackI
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.system.NativeType
import kotlin.coroutines.experimental.CoroutineContext

class RenderGameSystem(coroutineContext: CoroutineContext): GameSystem(coroutineContext) {
  suspend override fun invoke(event: Event) {
    when (event) {
      is Initialize -> initialize()
      is Update -> update(event)
      is Render -> render(event)
      is Shutdown -> shutdown()
    }
  }

  private var window: Long = 0

  private fun initialize() {
    GLFWErrorCallback.createPrint(System.err).set()

    // GameInitializationEvent GLFW. Most GLFW functions will not work before doing this.
    if (!glfwInit())
      throw IllegalStateException("Unable to initialize GLFW")

    // Configure GLFW
    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    // Create the window
    val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())
    glfwWindowHint(GLFW_RED_BITS, vidmode.redBits())
    glfwWindowHint(GLFW_GREEN_BITS, vidmode.greenBits())
    glfwWindowHint(GLFW_BLUE_BITS, vidmode.blueBits())
    glfwWindowHint(GLFW_REFRESH_RATE, vidmode.refreshRate())
    val screenWidth = vidmode.width() / 2
    val screenHeight = vidmode.height() / 2
    window = glfwCreateWindow(screenWidth, screenHeight, "Fractured Skies", NULL, NULL)
    if (window == NULL)
      throw RuntimeException("Failed to create the GLFW window")

    glfwSetKeyCallback(window, this::keyCallback)
    glfwSetCharModsCallback(window, this::charModsCallback)
    glfwSetCursorPosCallback(window, this::cursorPosCallback)
    glfwSetMouseButtonCallback(window, this::mouseButtonCallback)
    glfwSetWindowSizeCallback(window, this::windowSizeCallback)

    glfwSetInputMode(window, GLFW_STICKY_KEYS, 1)
    glfwSetWindowPos(window, (vidmode.width() - screenWidth) / 2, (vidmode.height() - screenHeight) / 2)

    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)
    glfwShowWindow(window)
    GL.createCapabilities()
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
  }

  private suspend fun update(event: Update) {
    glfwPollEvents()
    if (glfwWindowShouldClose(window)) {
      publish(RequestShutdown(Cause.of(event.cause, this), event.context))
    }
  }

  private fun render(event: Render) {
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glEnable(GL_CULL_FACE)
    glCullFace(GL_FRONT)

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
}