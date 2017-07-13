package fs.client.ui.game;

import fs.client.event.BlockAddedEvent;
import fs.client.event.BlockRemovedEvent;
import fs.client.event.BlockUpdatedEvent;
import fs.client.ui.Component;
import fs.client.ui.event.UIEvent;
import fs.client.ui.event.MouseDown;
import fs.client.ui.primitive.mesh.Mesh;
import fs.client.ui.primitive.mesh.MeshRenderer;
import fs.client.ui.primitive.mesh.Program;
import fs.client.ui.primitive.mesh.TextureArray;
import fs.client.world.*;
import fs.math.Matrix4;
import fs.math.Quaternion4;
import fs.math.Vector3;
import fs.math.Vector4;
import org.lwjgl.glfw.GLFW;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Optional;

import static fs.math.Matrix4.*;
import static fs.math.Quaternion4.quat4;
import static fs.math.Vector3.vec3;
import static fs.math.Vector4.vec4;
import static fs.util.ResourceLoader.loadAsByteBuffer;
import static fs.util.ResourceLoader.loadAsString;

@Singleton
public class WorldRenderer extends Component {

  @Inject
  private javax.enterprise.event.Event<Object> events;

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

  private Mesh blockMesh;
  private MeshRenderer blockRenderer;

  private Mesh waterMesh;
  private MeshRenderer waterRenderer;

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
    model(vec3(-(world.width() / 2), 0f, -(world.height() / 2)));
    view(vec3(0.0f, 12f, -3f * world.depth()), quat4(vec3(1, 0, 0), (float) Math.PI / 4f));

    // Draw world
    dirty();
  }

  private WorldRenderer model(Vector3 position) {
    modelPosition.set(position);
    model.set(mat4(position));

    return this;
  }

  private void dirty() {
    blockMesh = WorldMeshGenerator.generateMesh(world);
    blockRenderer = null;

    waterMesh = WaterMeshGenerator.generateMesh(world);
    waterRenderer = null;
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
    if (blockRenderer == null)
      blockRenderer = new MeshRenderer(blockMesh, program, textureArray, model, view, projection);
    blockRenderer
        .bounds(x, y, width, height)
        .render();

    if (waterRenderer == null)
      waterRenderer = new MeshRenderer(waterMesh, program, textureArray, model, view, projection);
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
    if (event instanceof MouseDown) {
      int mx = ((MouseDown) event).x();
      int my = ((MouseDown) event).y();
      BlockRayHit blockRay = pick(mx, my).get().orElse(null);
      if (blockRay != null) {
        Location blockLocation = blockRay.location();
        if (((MouseDown) event).button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
          Location location = blockRay.faces().get(0).neighbour(blockLocation);
          if (location.isWithinWorldLimits()) {
            BlockAddedEvent addBlockEvent = new BlockAddedEvent(location, BlockType.BLOCK);
            events.fire(addBlockEvent);
            if (!addBlockEvent.isCancelled()) {
              BlockState block = location.block();
              block.type(BlockType.BLOCK);
              block.waterLevel(0);
            }
          }
        } else {
          BlockRemovedEvent removeBlockEvent = new BlockRemovedEvent(blockLocation);
          events.fire(removeBlockEvent);
          if (!removeBlockEvent.isCancelled()) {
            blockLocation.block().type(BlockType.AIR);
          }
        }
      }
    }
  }

  private BlockRay pick(int mx, int my) {
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

    Vector4 direction = vec4(rayEnd).subtract(rayStart).normalize();
    return new BlockRay(
        world,
        vec3(rayStart).subtract(modelPosition),
        vec3(direction),
        BlockRay.FILTER_AIR,
        BlockRay.FILTER_NONE
    );
  }
}
