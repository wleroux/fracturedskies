package fs.client.ui.game;

import fs.client.event.BlockUpdatedEvent;
import fs.client.event.WaterUpdatedEvent;
import fs.client.ui.Component;
import fs.client.ui.event.*;
import fs.client.ui.primitive.mesh.Mesh;
import fs.client.ui.primitive.mesh.MeshRenderer;
import fs.client.ui.primitive.mesh.Program;
import fs.client.ui.primitive.mesh.TextureArray;
import fs.client.world.WaterMeshGenerator;
import fs.client.world.World;
import fs.client.world.WorldMeshGenerator;
import fs.math.Matrix4;
import fs.math.Quaternion4;
import fs.math.Vector3;
import fs.math.Vector4;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import static fs.math.Matrix4.*;
import static fs.math.Quaternion4.quat4;
import static fs.math.Vector3.vec3;
import static fs.math.Vector4.vec4;
import static fs.util.ResourceLoader.loadAsByteBuffer;
import static fs.util.ResourceLoader.loadAsString;
import static org.lwjgl.glfw.GLFW.*;

@Singleton
public class WorldRenderer extends Component {

  @Inject
  private WorldController controller;

  @Inject
  private World world;

  private final Program program;
  private final TextureArray textureArray;

  private final Matrix4 projection = mat4();
  private final Matrix4 inverseProjection = mat4();

  private final Vector3 viewPosition = vec3();
  private final Matrix4 view = mat4();
  private final Matrix4 inverseView = mat4();

  private final Vector3 modelPosition = vec3();
  private final Matrix4 model = mat4();

  private int screenWidth;
  private int screenHeight;

  private MeshRenderer blockRenderer;
  private MeshRenderer waterRenderer;
  private long window;

  public WorldRenderer() {
    ClassLoader classLoader = this.getClass().getClassLoader();
    program = new Program(
        loadAsString("fs/client/ui/primitive/mesh/default.vs", classLoader),
        loadAsString("fs/client/ui/primitive/mesh/default.fs", classLoader)
    );

    textureArray = new TextureArray(
        loadAsByteBuffer("fs/client/ui/primitive/mesh/tileset.png", classLoader),
        16,
        16,
        3
    );
  }

  public void setScreenDimensions(int screenWidth, int screenHeight) {
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;

    projection(orthogonal(-10, 10, -10, 10, 0.03f, 1000f));
    projection(perspective((float) Math.PI / 4, screenWidth, screenHeight, 0.03f, 1000f));
  }

  private WorldRenderer projection(Matrix4 mat4) {
    projection.set(mat4);
    inverseProjection.set(mat4).invert();

    return this;
  }

  private WorldRenderer view(Vector3 position, Quaternion4 rotation) {
    viewPosition.set(position);
    inverseView.set(mat4(viewPosition, rotation));
    view.set(inverseView).invert();

    return this;
  }

  public void onWorldUpdated(@Observes BlockUpdatedEvent event) {
    if (event instanceof WaterUpdatedEvent) {
      waterRenderer = null;
    } else {
      blockRenderer = null;
    }
  }

  private WorldRenderer model(Vector3 position) {
    modelPosition.set(position);
    model.set(mat4(position));

    return this;
  }

  @Override
  public int preferredWidth() {
    return screenWidth;
  }

  @Override
  public int preferredHeight() {
    return screenHeight;
  }

  @Override
  public WorldRenderer bounds(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;

    return this;
  }

  @Override
  public void render() {
    view(controller.view().position(), controller.view().rotation());

    if (blockRenderer == null) {
      Mesh blockMesh = WorldMeshGenerator.generateMesh(world);
      blockRenderer = new MeshRenderer(blockMesh, program, textureArray, model, view, projection);
    }
    blockRenderer
        .bounds(x, y, width, height)
        .render();

    if (waterRenderer == null) {
      Mesh waterMesh = WaterMeshGenerator.generateMesh(world);
      waterRenderer = new MeshRenderer(waterMesh, program, textureArray, model, view, projection);
    }
    waterRenderer
        .bounds(x, y, width, height)
        .render();
  }

  @Override
  public Component findComponentAt(int x, int y) {
    if (this.x <= x && x <= this.x + this.width) {
      if (this.y <= y && y <= this.y + this.height) {
        return this;
      }
    }
    return null;
  }

  public void handle(UIEvent event) {
    if (event instanceof FocusIn) {
      // Reset mouse cursor
      glfwSetCursorPos(window, screenWidth / 2, screenHeight / 2);
      glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    } else if (event instanceof FocusOut) {
      glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    } else if (event instanceof MouseDown) {
      onMouseDown((MouseDown) event);
    } else if (event instanceof MouseMove) {
      onMouseMove((MouseMove) event);
    } else if (event instanceof Key) {
      onKey((Key) event);
    }
  }

  private static float PITCH_SENSITIVITY = 4;
  private static float YAW_SENSITIVITY = 4;
  private float pitch = PITCH_SENSITIVITY / 4;
  private float yaw = 0;
  private static Quaternion4 ROTATE_UP = quat4(vec3(1, 0, 0), (float) Math.PI / PITCH_SENSITIVITY);
  private static Quaternion4 ROTATE_LEFT = quat4(vec3(0, 1, 0), (float) Math.PI / YAW_SENSITIVITY);
  private void onMouseMove(MouseMove event) {
    if (!root().isFocused(this)) {
      return;
    }

    // Calculate new Yaw / Pitch based on mouse movement
    float dx = (event.x() / (float) (screenWidth / 2)) - 1;
    yaw = (yaw + dx) % (2f * YAW_SENSITIVITY);

    float dy = (event.y() / (float) (screenHeight / 2)) - 1;
    pitch = Math.max(-PITCH_SENSITIVITY / 2f, Math.min(pitch + dy, PITCH_SENSITIVITY / 2f));
    controller.view().rotation()
        .set(quat4(ROTATE_UP).multiply(pitch))
        .multiply(quat4(ROTATE_LEFT).multiply(yaw));

    // Reset mouse cursor
    glfwSetCursorPos(window, screenWidth / 2, screenHeight / 2);
  }

  private void onKey(Key event) {
    controller.onKeyDown(event);
  }

  private void onMouseDown(MouseDown event) {
    event.stopPropagation();
    if (!root().isFocused(this)) {
      root().focus(this);
      return;
    }

    int mx = event.x();
    int my = event.y();

    Vector4 rayStart = vec4(
        -((float) (screenWidth - mx) / (float) screenWidth - 0.5f) * 2,
        ((float) my / (float) screenHeight - 0.5f) * 2,
        -1f,
        1f
    )
        .multiply(inverseProjection)
        .multiply(inverseView);
    rayStart.multiply(1f / rayStart.w());

    Vector4 rayEnd = vec4(
        -((float) (screenWidth - mx) / (float) screenWidth - 0.5f) * 2,
        ((float) my / (float) screenHeight - 0.5f) * 2,
        1f,
        1f
    )
        .multiply(inverseProjection)
        .multiply(inverseView);
    rayEnd.multiply(1f / rayEnd.w());

    controller.onMouseDown(event, vec3(rayStart).subtract(modelPosition), vec3(rayEnd).subtract(modelPosition));
  }

  public void setWindow(long window) {
    this.window = window;
  }
}
