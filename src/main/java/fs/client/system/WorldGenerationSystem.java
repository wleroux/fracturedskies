package fs.client.system;

import fs.client.event.GameInitializationEvent;
import fs.client.event.BlockGeneratedEvent;
import fs.client.world.BlockType;
import fs.client.world.BlockState;
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
        world.block(ix, 0, iz).type(BlockType.BLOCK);
      }

      world.block(ix, 1, 1).type(BlockType.BLOCK);
    }

    world.block(1, 0, 0).type(null);
    world.block(1, 0, 1).type(null);
    world.block(1, 0, 2).type(null);

    events.fire(new BlockGeneratedEvent());
  }
}
