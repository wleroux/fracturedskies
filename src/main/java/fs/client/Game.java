package fs.client;

import fs.client.world.World;
import fs.math.Transform;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import static fs.math.Quaternion4.quat4;
import static fs.math.Vector3.vec3;

/**
 *
 */
@Singleton
public class Game {

  public static final int WORLD_WIDTH = 64;
  public static final int WORLD_HEIGHT = WORLD_WIDTH;
  public static final int WORLD_DEPTH = WORLD_WIDTH;

  private World world = new World(WORLD_WIDTH, WORLD_HEIGHT, WORLD_DEPTH);

  private Transform view = new Transform(vec3(WORLD_WIDTH/2, WORLD_HEIGHT, 0), quat4(vec3(1, 0, 0), (float) Math.PI / 4f));

  @Produces
  public World world() {
    return world;
  }

  public Transform view() {
    return view;
  }
}
