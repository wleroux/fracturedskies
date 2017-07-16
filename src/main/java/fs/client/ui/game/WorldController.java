package fs.client.ui.game;

import fs.client.Game;
import fs.client.event.BlockAddedEvent;
import fs.client.event.BlockRemovedEvent;
import fs.client.ui.event.Key;
import fs.client.ui.event.MouseDown;
import fs.client.world.*;
import fs.math.Quaternion4;
import fs.math.Transform;
import fs.math.Vector3;
import org.lwjgl.glfw.GLFW;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

import static fs.math.Quaternion4.quat4;
import static fs.math.Vector3.vec3;
import static org.lwjgl.glfw.GLFW.*;

@Singleton
public class WorldController {

  @Inject
  private Event<Object> events;

  @Inject
  private Game game;

  private static final Vector3 FORWARD = vec3(0, 0, 1);
  private static final Vector3 BACKWARDS = vec3(0, 0, -1);
  private static final Vector3 LEFT = vec3(-1, 0, 0);
  private static final Vector3 RIGHT = vec3(1, 0, 0);
  private static final Vector3 UP = vec3(0, 1, 0);
  private static final Vector3 DOWN = vec3(0, -1, 0);

  public void onKeyDown(Key key) {
    Quaternion4 movementRotation = quat4(game.view().rotation()).x(0).z(0).normalize();
    switch (key.key()) {
      case GLFW_KEY_UP:
      case GLFW_KEY_W:
        game.view().position().add(vec3(FORWARD).rotate(movementRotation));
        break;
      case GLFW_KEY_DOWN:
      case GLFW_KEY_S:
        game.view().position().add(vec3(BACKWARDS).rotate(movementRotation));
        break;
      case GLFW_KEY_LEFT:
      case GLFW_KEY_A:
        game.view().position().add(vec3(LEFT).rotate(movementRotation));
        break;
      case GLFW_KEY_RIGHT:
      case GLFW_KEY_D:
        game.view().position().add(vec3(RIGHT).rotate(movementRotation));
        break;
      case GLFW_KEY_SPACE:
        game.view().position().add(vec3(UP));
        break;
      case GLFW_KEY_LEFT_SHIFT:
        game.view().position().add(vec3(DOWN));
        break;
    }
  }

  public Transform view() {
    return game.view();
  }

  public World world() {
    return game.world();
  }

  public void onMouseDown(MouseDown event, Vector3 rayStart, Vector3 rayEnd) {
    Vector3 direction = vec3(rayEnd).subtract(rayStart).normalize();
    Vector3 origin = vec3(rayStart);
    BlockRay blockRay = new BlockRay(
        game.world(),
        origin,
        vec3(direction),
        BlockRay.FILTER_AIR,
        BlockRay.FILTER_NONE
    );

    if (blockRay.hasNext()) {
      BlockRayHit blockRayHit = blockRay.next();

      Location blockLocation = blockRayHit.location();
      if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
        Location location = blockLocation.neighbour(blockRayHit.faces().get(0));
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
