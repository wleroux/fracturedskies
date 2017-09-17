package fs.controller;

import fs.block.BlockState;
import fs.block.BlockType;
import fs.Game;
import fs.event.BlockAddedEvent;
import fs.event.BlockRemovedEvent;
import fs.event.controller.ControllerKeyPressedEvent;
import fs.event.controller.ControllerKeyReleasedEvent;
import fs.event.controller.MouseClickEvent;
import fs.event.controller.MoveCameraEvent;
import fs.event.game.TickEvent;
import fs.world.*;
import fs.util.math.Quaternion4;
import fs.util.math.Vector3;
import org.lwjgl.glfw.GLFW;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import static fs.util.math.Quaternion4.quat4;
import static fs.util.math.Vector3.vec3;

@Singleton
public class ControllerSystem {

  @Inject
  private Game game;

  @Inject
  private Controller controller;

  @Inject
  private Event<Object> events;

  private static Quaternion4 ROTATE_UP = quat4(vec3(1, 0, 0), (float) Math.PI);
  private static Quaternion4 ROTATE_LEFT = quat4(vec3(0, 1, 0), (float) Math.PI);

  private static final Vector3 FORWARD = vec3(0, 0, 1);
  private static final Vector3 BACKWARDS = vec3(0, 0, -1);
  private static final Vector3 LEFT = vec3(-1, 0, 0);
  private static final Vector3 RIGHT = vec3(1, 0, 0);
  private static final Vector3 UP = vec3(0, 1, 0);
  private static final Vector3 DOWN = vec3(0, -1, 0);

  public void pressed(@Observes ControllerKeyPressedEvent keyPressedEvent) {
    controller.press(keyPressedEvent.getKey());
  }

  public void released(@Observes ControllerKeyReleasedEvent keyReleasedEvent) {
    controller.release(keyReleasedEvent.getKey());
  }

  public void moveCamera(@Observes TickEvent event) {
    boolean forward = controller.isPressed(Controller.Key.FORWARD);
    boolean backward = controller.isPressed(Controller.Key.BACKWARDS);

    boolean left = controller.isPressed(Controller.Key.LEFT);
    boolean right = controller.isPressed(Controller.Key.RIGHT);

    boolean up = controller.isPressed(Controller.Key.UP);
    boolean down = controller.isPressed(Controller.Key.DOWN);

    Quaternion4 movementRotation = quat4(game.view().rotation()).x(0).z(0).normalize();
    if (forward && ! backward) {
      game.view().position().add(vec3(FORWARD).rotate(movementRotation));
    } else if (backward && ! forward) {
      game.view().position().add(vec3(BACKWARDS).rotate(movementRotation));
    }

    if (left && ! right) {
      game.view().position().add(vec3(LEFT).rotate(movementRotation));
    } else if (right && !left) {
      game.view().position().add(vec3(RIGHT).rotate(movementRotation));
    }

    if (up && !down) {
      game.view().position().add(vec3(UP));
    } else if (down && !up) {
      game.view().position().add(vec3(DOWN));
    }
  }

  public void mouseClick(@Observes MouseClickEvent mouseClickEvent) {
    Vector3 direction = vec3(mouseClickEvent.getRayEnd()).subtract(mouseClickEvent.getRayStart()).normalize();
    Vector3 origin = vec3(mouseClickEvent.getRayStart());
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
      if (mouseClickEvent.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
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

  public void lookAround(@Observes MoveCameraEvent moveCameraEvent) {
    game.view().rotation()
        .set(quat4(ROTATE_UP).multiply(moveCameraEvent.getPitch()))
        .multiply(quat4(ROTATE_LEFT).multiply(moveCameraEvent.getYaw()));
  }
}
