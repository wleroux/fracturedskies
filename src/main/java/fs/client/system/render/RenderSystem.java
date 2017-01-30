package fs.client.system.render;

import fs.client.async.Dispatcher;
import fs.client.async.GameSystem;
import fs.client.event.*;
import fs.client.ui.game.WorldRenderer;
import fs.client.ui.layout.Card;
import fs.client.ui.layout.Flex;
import fs.client.ui.primitive.button.Button;
import fs.client.world.World;
import fs.math.Matrix4;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.util.concurrent.CompletableFuture;

import static fs.client.ui.layout.Flex.Direction.ROW;
import static fs.client.ui.layout.Flex.JustifyContent.CENTER;
import static fs.client.ui.layout.Flex.Wrap.NO_WRAP;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class RenderSystem implements GameSystem {

	private final Dispatcher dispatcher;

	private Card card = new Card();
	private WorldRenderer worldRenderer;
	private World world;
    private int screenWidth;
    private int screenHeight;

    public RenderSystem(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public boolean canHandle(Object event) {
		return event instanceof Initialized ||
				event instanceof  UpdateRequested ||
				event instanceof  RenderRequested ||
				event instanceof  Terminated ||
				event instanceof WorldGenerated ||
				event instanceof WaterLevelsUpdated;
	}

	@Override
	public void accept(Object o, CompletableFuture<Void> future) {
		if (o instanceof Initialized) {
			init();
			future.complete(null);
		} else if (o instanceof WorldGenerated) {
			world = ((WorldGenerated) o).world();
			if (worldRenderer != null) {
                worldRenderer.setWorld(((WorldGenerated) o).world());
            }

			future.complete(null);
		} else if (o instanceof WaterLevelsUpdated) {
			world.waterLevel(((WaterLevelsUpdated) o).waterLevel());
			if (worldRenderer != null) {
                worldRenderer.setWorld(world);
            }
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
		screenWidth = vidmode.width() / 2;
		screenHeight = vidmode.height() / 2;
        window = glfwCreateWindow(screenWidth, screenHeight, "Fractured Skies", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		glfwSetWindowPos(window, (vidmode.width() - screenWidth) / 2, (vidmode.height() - screenHeight) / 2);
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true);
		});

		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
		GL.createCapabilities();
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Initialize Renderer
        worldRenderer = new WorldRenderer(screenWidth, screenHeight);
        card.add(worldRenderer);
        if (world != null) {
            worldRenderer.setWorld(world);
        }

        Flex flex = new Flex();
        flex.direction(ROW);
        flex.justifyContent(CENTER);
        flex.alignItems(Flex.ItemAlign.CENTER);
        flex.alignContent(Flex.ContentAlign.CENTER);
        flex.wrap(NO_WRAP);

		Matrix4 guiProjection = Matrix4.orthogonal(0, screenWidth, screenHeight, 0, 0.03f, 1000f);
		for (int i = 1; i <= 1; i ++) {
            Button button = new Button(guiProjection)
                    .text("Hello, World!");
            flex.add(button);
        }

		card.add(flex);
    }

    private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);

		card.render(0, 0, screenWidth, screenHeight);

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
		worldRenderer.update();

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
