package fs.render;

import fs.event.game.GamePreInitializationEvent;
import fs.event.game.RenderEvent;
import fs.event.game.TerminatedEvent;
import fs.render.ui.game.WorldRenderer;
import fs.render.ui.primitive.Root;
import fs.world.World;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Singleton;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@Singleton
public class RenderSystem {

  @Inject
  private Event<Object> events;

  private Root root = new Root();
  private WorldRenderer worldRenderer;

  @Inject
  private World world;

  private int screenWidth;
  private int screenHeight;
  private long window;

  public void onGamePreInitializationEvent(@Observes GamePreInitializationEvent event) {
    GLFWErrorCallback.createPrint(System.err).set();

    // GameInitializationEvent GLFW. Most GLFW functions will not work before doing this.
    if (!glfwInit())
      throw new IllegalStateException("Unable to initialize GLFW");

    // Configure GLFW
    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

    // Create the window
    GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    glfwWindowHint(GLFW_RED_BITS, vidmode.redBits());
    glfwWindowHint(GLFW_GREEN_BITS, vidmode.greenBits());
    glfwWindowHint(GLFW_BLUE_BITS, vidmode.blueBits());
    glfwWindowHint(GLFW_REFRESH_RATE, vidmode.refreshRate());
    screenWidth = vidmode.width() / 2;
    screenHeight = vidmode.height() / 2;
    window = glfwCreateWindow(screenWidth, screenHeight, "Fractured Skies", NULL, NULL);
    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window");

    glfwSetInputMode(window, GLFW_STICKY_KEYS, 1);
    glfwSetWindowPos(window, (vidmode.width() - screenWidth) / 2, (vidmode.height() - screenHeight) / 2);
    glfwSetKeyCallback(window, root::onKey);
    glfwSetCharModsCallback(window, root::onCharMods);
    glfwSetCursorPosCallback(window, root::onCursorPos);
    glfwSetMouseButtonCallback(window, root::onMouseButton);
    glfwSetWindowSizeCallback(window, root::onWindowSize);
    root.onWindowSize(window, screenWidth, screenHeight);

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    glfwShowWindow(window);
    GL.createCapabilities();
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    // Initialize Renderer
    worldRenderer = CDI.current().select(WorldRenderer.class).get();
    worldRenderer.setWindow(window);
    worldRenderer.setScreenDimensions(screenWidth, screenHeight);
    root.add(worldRenderer);
  }

  public void onRenderEvent(@Observes RenderEvent event) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_FRONT);

    root
        .bounds(0, 0, screenWidth, screenHeight)
        .render();

    glfwSwapBuffers(window); // swap the color buffers
    glfwPollEvents();

    if (glfwWindowShouldClose(window)) {
      events.fire(new TerminatedEvent());

      // Free the window callbacks and destroy the window
      glfwFreeCallbacks(window);
      glfwDestroyWindow(window);

      // Terminate GLFW and free the error callback
      glfwTerminate();
      glfwSetErrorCallback(null).free();
    }
  }
}
