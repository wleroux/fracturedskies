package fs.client.system;

import fs.client.event.GameInitializationEvent;
import fs.client.event.BlockGeneratedEvent;
import fs.client.world.Tile;
import fs.client.world.World;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Random;

public class WorldGenerationSystem {
  private static final int SEED = new Random().nextInt();

  @Inject
  private Event<Object> events;

  @Inject
  private World world;

  // private static final WorldGenerator worldGenerator = new WorldGenerator(WORLD_WIDTH, WORLD_HEIGHT, WORLD_DEPTH);

  public void onInitialized(@Observes GameInitializationEvent event) {
    for (int ix = 0; ix < world.width(); ix++) {
      for (int iz = 0; iz < world.depth(); iz++) {
        world.setBlock(world.converter().index(ix, 0, iz), Tile.BLOCK);
      }

      world.setBlock(world.converter().index(ix, 1, 1), Tile.BLOCK);
    }

    world.setBlock(world.converter().index(1, 0, 0), null);
    world.setBlock(world.converter().index(1, 0, 1), null);
    world.setBlock(world.converter().index(1, 0, 2), null);

    events.fire(new BlockGeneratedEvent());
  }
}
