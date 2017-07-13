package fs.client;

import fs.client.world.World;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class Game {

  public static final int WORLD_WIDTH = 4;
  public static final int WORLD_HEIGHT = WORLD_WIDTH;
  public static final int WORLD_DEPTH = WORLD_WIDTH;

  private World world = new World(WORLD_WIDTH, WORLD_HEIGHT, WORLD_DEPTH);

  @Produces
  public World getWorld() {
    return world;
  }
}
