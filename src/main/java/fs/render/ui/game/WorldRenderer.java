package fs.render.ui.game;

import fs.Game;
import fs.event.LocationUpdatedEvent;
import fs.event.WaterUpdatedEvent;
import fs.event.WorldGeneratedEvent;
import fs.event.controller.ControllerKeyPressedEvent;
import fs.event.controller.ControllerKeyReleasedEvent;
import fs.event.controller.MouseClickEvent;
import fs.event.controller.MoveCameraEvent;
import fs.controller.Controller;
import fs.render.Component;
import fs.render.ui.primitive.OpenGLComponent;
import fs.render.ui.primitive.mesh.Mesh;
import fs.render.ui.primitive.mesh.Program;
import fs.render.ui.primitive.mesh.TextureArray;
import fs.render.event.*;
import fs.util.math.*;
import fs.world.Location;
import fs.world.World;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import static fs.Game.CHUNK_SIZE;
import static fs.util.math.Matrix4.*;
import static fs.util.math.Quaternion4.quat4;
import static fs.util.math.Vector3.vec3;
import static fs.util.math.Vector4.vec4;
import static fs.util.ResourceLoader.loadAsByteBuffer;
import static fs.util.ResourceLoader.loadAsString;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.glUseProgram;

@Singleton
public class WorldRenderer extends OpenGLComponent {

  @Inject
  private World world;

  @Inject
  private Game game;

  @Inject
  private Event<Object> events;

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

  private long window;
  private Map<Integer, Mesh> blockMesh = new HashMap<>();
  private Map<Integer, Mesh> waterMesh = new HashMap<>();

  public WorldRenderer() {
    ClassLoader classLoader = this.getClass().getClassLoader();
    program = new Program(
        loadAsString("fs/render/ui/primitive/mesh/default.vs", classLoader),
        loadAsString("fs/render/ui/primitive/mesh/default.fs", classLoader)
    );

    textureArray = new TextureArray(
        loadAsByteBuffer("fs/render/ui/primitive/mesh/tileset.png", classLoader),
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

  public void onWorldUpdated(@Observes LocationUpdatedEvent event) {
    Location updatedWaterBlock = event.location();
    CoordinateConverter converter = new CoordinateConverter(world.width() / CHUNK_SIZE, world.height() / CHUNK_SIZE, world.depth() / CHUNK_SIZE);
    for (Location neighbourLocation: updatedWaterBlock.neighbours().values()) {
      int chunkX = neighbourLocation.x() / CHUNK_SIZE;
      int chunkY = neighbourLocation.y() / CHUNK_SIZE;
      int chunkZ = neighbourLocation.z() / CHUNK_SIZE;
      int chunkIndex = converter.index(chunkX, chunkY, chunkZ);

      if (event instanceof WaterUpdatedEvent) {
        waterMesh.remove(chunkIndex);
      }  else {
        blockMesh.remove(chunkIndex);
        waterMesh.remove(chunkIndex);
      }
    }
  }

  public void onWorldGenerated(WorldGeneratedEvent event) {
    waterMesh.clear();
    blockMesh.clear();
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
    view(game.view().position(), game.view().rotation());

    glUseProgram(program.id());
    glEnable(GL_DEPTH_TEST);
    uniform(MODEL_LOCATION, model);
    uniform(VIEW_LOCATION, view);
    uniform(PROJECTION_LOCATION, projection);
    uniform(ALBEDO_LOCATION, GL_TEXTURE0, textureArray);

    CoordinateConverter converter = new CoordinateConverter(world.width() / CHUNK_SIZE, world.height() / CHUNK_SIZE, world.depth() / CHUNK_SIZE);
    for (int iz = 0; iz < world.depth() / CHUNK_SIZE; iz ++) {
      for (int ix = 0; ix < world.width() / CHUNK_SIZE; ix++) {
        for (int iy = 0; iy < world.height() / CHUNK_SIZE; iy++) {
          int cacheIndex = converter.index(ix, iy, iz);
          if (!blockMesh.containsKey(cacheIndex)) {
            blockMesh.put(cacheIndex, WorldMeshGenerator.generateMesh(world, ix, iy, iz));
          }
          draw(blockMesh.get(cacheIndex));
        }
      }
    }

    for (int iz = 0; iz < world.depth() / CHUNK_SIZE; iz ++) {
      for (int ix = 0; ix < world.width() / CHUNK_SIZE; ix++) {
        for (int iy = 0; iy < world.height() / CHUNK_SIZE; iy++) {
          int cacheIndex = converter.index(ix, iy, iz);
          if (!waterMesh.containsKey(cacheIndex)) {
            waterMesh.put(cacheIndex, WaterMeshGenerator.generateMesh(world, ix, iy, iz));
          }
          draw(waterMesh.get(cacheIndex));
        }
      }
    }
    glUseProgram(0);
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
      fireControllerKeyEvent((Key) event);
    }
  }
  private static float YAW_SENSITIVITY = 1;
  private static float PITCH_SENSITIVITY = 1;
  private float pitch = PITCH_SENSITIVITY / 4;
  private float yaw = 0;

  private void onMouseMove(MouseMove event) {
    if (!root().isFocused(this)) {
      return;
    }

    // Calculate new Yaw / Pitch based on mouse movement
    float dx = (event.x() / (float) (screenWidth / 2)) - 1;
    yaw = (yaw + dx) % (2f * YAW_SENSITIVITY);

    float dy = (event.y() / (float) (screenHeight / 2)) - 1;
    pitch = Math.max(-PITCH_SENSITIVITY / 2f, Math.min(pitch + dy, PITCH_SENSITIVITY / 2f));

    // Reset mouse cursor
    glfwSetCursorPos(window, screenWidth / 2, screenHeight / 2);

    events.fire(new MoveCameraEvent(pitch, yaw));
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

    fireMouseDownEvent(event, vec3(rayStart).subtract(modelPosition), vec3(rayEnd).subtract(modelPosition));
  }


  public void fireControllerKeyEvent(Key key) {
    Controller.Key controllerKey = controllerKeyFor(key);
    if (controllerKey != null) {
      if (key.action() == GLFW_RELEASE) {
        events.fire(new ControllerKeyReleasedEvent(controllerKey));
      } else if (key.action() == GLFW_PRESS) {
        events.fire(new ControllerKeyPressedEvent(controllerKey));
      }
    }
  }

  private Controller.Key controllerKeyFor(Key key) {
    switch (key.key()) {
      case GLFW_KEY_UP:
      case GLFW_KEY_W:
        return Controller.Key.FORWARD;
      case GLFW_KEY_DOWN:
      case GLFW_KEY_S:
        return Controller.Key.BACKWARDS;
      case GLFW_KEY_LEFT:
      case GLFW_KEY_A:
        return Controller.Key.LEFT;
      case GLFW_KEY_RIGHT:
      case GLFW_KEY_D:
        return Controller.Key.RIGHT;
      case GLFW_KEY_SPACE:
        return Controller.Key.UP;
      case GLFW_KEY_LEFT_SHIFT:
        return Controller.Key.DOWN;
      default:
        return null;
    }
  }

  public void fireMouseDownEvent(MouseDown event, Vector3 rayStart, Vector3 rayEnd) {
    events.fire(new MouseClickEvent(event.button(), rayStart, rayEnd));
  }

  public void setWindow(long window) {
    this.window = window;
  }
}
