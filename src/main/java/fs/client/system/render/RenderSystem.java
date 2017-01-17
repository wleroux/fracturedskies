package fs.client.system.render;

import fs.client.async.Dispatcher;
import fs.client.async.GameSystem;
import fs.client.event.*;
import fs.client.gl.Mesh;
import fs.client.gl.Program;
import fs.client.gl.TextureArray;
import fs.client.ui.label.Label;
import fs.client.ui.label.LabelMeshGenerator;
import fs.client.ui.label.LabelRenderer;
import fs.client.world.WaterMeshGenerator;
import fs.client.world.World;
import fs.client.world.WorldMeshGenerator;
import fs.math.Matrix4;
import fs.math.Quaternion4;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.util.concurrent.CompletableFuture;

import static fs.math.Color4.color;
import static fs.math.Matrix4.mat4;
import static fs.math.Matrix4.perspective;
import static fs.math.Quaternion4.quat4;
import static fs.math.Vector3.vec3;
import static fs.util.ResourceLoader.loadAsByteBuffer;
import static fs.util.ResourceLoader.loadAsString;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class RenderSystem implements GameSystem {

	private final Dispatcher dispatcher;
    private Program program;
	private Mesh blockMesh;
	private Mesh waterMesh;
	private Matrix4 model;
    private Matrix4 view;
    private Matrix4 projection;
	private TextureArray textureArray;

	private int tickCount = 0;
	private World world;
	private Matrix4 labelModel;
	private Matrix4 labelView;
	private Matrix4 labelProjection;
	private Label label;
	private Program labelProgram;
	private TextureArray labelTexture;

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
			world = new World(((WorldGenerated) o).world());
			model = mat4(vec3(-(world.width() / 2), 0f, -(world.height() / 2)));
			future.complete(null);
		} else if (o instanceof WaterLevelsUpdated) {
			world.waterLevel(((WaterLevelsUpdated) o).waterLevel());
			waterMesh = null;
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
		int width = vidmode.width() / 2;
		int height = vidmode.height() / 2;
        window = glfwCreateWindow(width, height, "Fractured Skies", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
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
        ClassLoader classLoader = getClass().getClassLoader();
        program = new Program(
                loadAsString("fs/client/gl/default.vs", classLoader),
                loadAsString("fs/client/gl/default.fs", classLoader)
        );

		textureArray = new TextureArray(
				loadAsByteBuffer("fs/client/gl/tileset.png", classLoader),
				16,
				16,
				3
		);

        projection = perspective((float) Math.PI / 4, width, height, 0.03f, 1000f);

        String missing = "~";
        label = new Label("Hello World!", color(0.4f, 0.4f, 0.9f, 1f));
        labelProgram = new Program(
				loadAsString("fs/client/ui/label/label.vs", classLoader),
				loadAsString("fs/client/ui/label/label.fs", classLoader)
		);
        labelModel = mat4(vec3(width / 2, height / 2, -1));
        labelView = mat4(vec3(0, 0, 0)).invert();
        labelProjection = Matrix4.orthogonal(0, width, 0, height, 0.03f, 1000f);
		labelTexture = new TextureArray(
				loadAsByteBuffer("fs/client/ui/label/font.png", classLoader),
				8,
				9,
				128
		);
    }

    private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_DEPTH_TEST);
//        glEnable(GL_CULL_FACE);
//        glCullFace(GL_FRONT);

		// Draw world
		if (world != null) {
			if (blockMesh == null) {
				blockMesh = WorldMeshGenerator.generateMesh(world);
			}
			if (waterMesh == null) {
				waterMesh = WaterMeshGenerator.generateMesh(world);
			}


			Quaternion4 rotation = quat4(vec3(0, 1, 0), (float) Math.PI * ((float) tickCount / 360f));
			view = mat4(vec3(0.0f, 5f, -3f * world.depth()).rotate(rotation), rotation).invert();

			MeshRenderer.render(blockMesh, program, textureArray, model, view, projection);
			MeshRenderer.render(waterMesh, program, textureArray, model, view, projection);
		}

		if (label != null) {
			Mesh labelMesh = LabelMeshGenerator.generate(label.text());
			LabelRenderer.render(labelMesh, label.color(), labelProgram, labelTexture, labelModel, labelView, labelProjection );
		}

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
		tickCount ++;

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
