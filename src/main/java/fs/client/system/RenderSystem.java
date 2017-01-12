package fs.client.system;

import fs.client.async.Dispatcher;
import fs.client.async.GameSystem;
import fs.client.event.*;
import fs.client.gl.Mesh;
import fs.client.gl.Program;
import fs.client.gl.TextureArray;
import fs.math.Matrix4;
import fs.math.Quaternion4;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.util.concurrent.CompletableFuture;

import static fs.math.Matrix4.mat4;
import static fs.math.Matrix4.perspective;
import static fs.math.Quaternion4.quat4;
import static fs.math.Vector3.vec3;
import static fs.util.ResourceLoader.loadAsByteBuffer;
import static fs.util.ResourceLoader.loadAsString;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class RenderSystem implements GameSystem {

	private final Dispatcher dispatcher;
    private Program program;
    private Mesh mesh;
	private Matrix4 model;
    private Matrix4 view;
    private Matrix4 projection;
	private TextureArray textureArray;

	private int tickCount = 0;

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

        mesh = new Mesh(new float[] {
                // front
                -0.5f,  0.5f, -0.5f,  0f, 0f, 0f,   0f, -1f,  0f,
                 0.5f,  0.5f, -0.5f,  0f, 1f, 0f,   0f, -1f,  0f,
                 0.5f, -0.5f, -0.5f,  1f, 1f, 0f,   0f, -1f,  0f,
                -0.5f, -0.5f, -0.5f,  1f, 0f, 0f,   0f, -1f,  0f,

                // top
                -0.5f, 0.5f,  0.5f,   0f, 0f, 1f,   0f,  1f,  0f,
                 0.5f, 0.5f,  0.5f,   0f, 1f, 1f,   0f,  1f,  0f,
                 0.5f, 0.5f, -0.5f,   1f, 1f, 1f,   0f,  1f,  0f,
                -0.5f, 0.5f, -0.5f,   1f, 0f, 1f,   0f,  1f,  0f,

                // left
                -0.5f,  0.5f,  0.5f,  0f, 0f, 0f,  -1f,  0f,  0f,
                -0.5f,  0.5f, -0.5f,  0f, 1f, 0f,  -1f,  0f,  0f,
                -0.5f, -0.5f, -0.5f,  1f, 1f, 0f,  -1f,  0f,  0f,
                -0.5f, -0.5f,  0.5f,  1f, 0f, 0f,  -1f,  0f,  0f,

                // right
                 0.5f,  0.5f, -0.5f,  0f, 0f, 0f,   1f,  0f,  0f,
                 0.5f,  0.5f,  0.5f,  0f, 1f, 0f,   1f,  0f,  0f,
                 0.5f, -0.5f,  0.5f,  1f, 1f, 0f,   1f,  0f,  0f,
                 0.5f, -0.5f, -0.5f,  1f, 0f, 0f,   1f,  0f,  0f,

                 // bottom
                -0.5f, -0.5f, -0.5f,   0f, 0f, 0f,  0f, -1f,  0f,
                 0.5f, -0.5f, -0.5f,   0f, 1f, 0f,  0f, -1f,  0f,
                 0.5f, -0.5f,  0.5f,   1f, 1f, 0f,  0f, -1f,  0f,
                -0.5f, -0.5f,  0.5f,   1f, 0f, 0f,  0f, -1f,  0f,

                // back
                 0.5f,  0.5f,  0.5f,   0f, 0f, 0f,  0f,  0f,  1f,
                -0.5f,  0.5f,  0.5f,   0f, 1f, 0f,  0f,  0f,  1f,
                -0.5f, -0.5f,  0.5f,   1f, 1f, 0f,  0f,  0f,  1f,
                 0.5f, -0.5f,  0.5f,   1f, 0f, 0f,  0f,  0f,  1f
        }, new int[] {
                0, 1, 2,
                2, 3, 0,

                4, 5, 6,
                6, 7, 4,

                8, 9, 10,
                10, 11, 8,

                12, 13, 14,
                14, 15, 12,

                16, 17, 18,
                18, 19, 16,

                20, 21, 22,
                22, 23, 20
        });
		textureArray = new TextureArray(
				loadAsByteBuffer("fs/client/gl/tileset.png", classLoader),
				16,
				16,
				2
		);

		model = mat4(vec3(0f, 0f, 0f));
        projection = perspective((float) Math.PI / 4, width, height, 0.03f, 1000f);
    }

    private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);

		// Draw square
		Quaternion4 rotation = quat4(vec3(0, 1, 0), (float) Math.PI * ((float) tickCount / 120f));
		view = mat4(vec3(0.0f, 1.5f, -10f).rotate(rotation), rotation).invert();
		drawSquare(mesh, program, textureArray, model, view, projection);

		glfwSwapBuffers(window); // swap the color buffers
		glfwPollEvents();
	}

    private final FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);

    private void drawSquare(Mesh mesh, Program program, TextureArray textureArray, Matrix4 model, Matrix4 view, Matrix4 projection) {
        glUseProgram(program.id());

        model.store(modelBuffer);
        modelBuffer.flip();
        glUniformMatrix4fv(0, false, modelBuffer);

        view.store(viewBuffer);
        viewBuffer.flip();
        glUniformMatrix4fv(1, false, viewBuffer);

        projection.store(projectionBuffer);
        projectionBuffer.flip();
        glUniformMatrix4fv(2, false, projectionBuffer);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D_ARRAY, textureArray.id());

        glBindVertexArray(mesh.vao());
        glDrawElements(GL_TRIANGLES, mesh.indexCount(), GL_UNSIGNED_INT, 0);
        glUseProgram(0);
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
