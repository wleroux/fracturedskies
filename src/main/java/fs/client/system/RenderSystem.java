package fs.client.system;

import fs.client.async.Dispatcher;
import fs.client.async.GameSystem;
import fs.client.event.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.concurrent.CompletableFuture;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class RenderSystem implements GameSystem {

	private final Dispatcher dispatcher;

	public RenderSystem(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public boolean canHandle(Object event) {
		return event instanceof Initialized ||
				event instanceof  UpdateRequested ||
				event instanceof  RenderRequested ||
				event instanceof  Terminated;
	}

	@Override
	public void accept(Object o, CompletableFuture<Void> future) {
		if (o instanceof Initialized) {
			init();
			future.complete(null);
		} else if (o instanceof UpdateRequested) {
			update(future);
		} else if (o instanceof RenderRequested) {
			render();
			future.complete(null);
		} else if (o instanceof Terminated) {
			terminate();
			future.complete(null);
		} else {
			future.complete(null);
		}
	}

	private long window;

	private void init() {
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialized GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
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
		window = glfwCreateWindow(vidmode.width() / 2, vidmode.height() / 2, "Fractured Skies", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		glfwSetWindowPos(window, vidmode.width() / 4, vidmode.height() / 4);
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true);
		});

		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
		GL.createCapabilities();
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
		glfwSwapBuffers(window); // swap the color buffers
		glfwPollEvents();
	}

	private void terminate() {
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void update(CompletableFuture<Void> future) {
		if (glfwWindowShouldClose(window)) {
			dispatcher.dispatch(new TerminationRequested(), future);
		} else {
			future.complete(null);
		}
	}

	@Override
	public String toString() {
		return "render";
	}
}
