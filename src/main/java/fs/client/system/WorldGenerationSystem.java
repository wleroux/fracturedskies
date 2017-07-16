package fs.client.system;

import fs.client.Game;
import fs.client.event.GameInitializationEvent;
import fs.client.event.WorldGeneratedEvent;
import fs.client.world.World;
import fs.client.world.WorldGenerator;

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

 private static final WorldGenerator worldGenerator = new WorldGenerator(Game.WORLD_WIDTH, Game.WORLD_HEIGHT, Game.WORLD_DEPTH);

  public void onInitialized(@Observes GameInitializationEvent event) {
    World generatedWorld = worldGenerator.generate(SEED);

    for (int ix = 0; ix < generatedWorld.width(); ix++) {
      for (int iz = 0; iz < generatedWorld.depth(); iz++) {
        for (int iy = 0; iy < generatedWorld.height(); iy ++) {
          world.block(ix, iy, iz).type(generatedWorld.block(ix, iy, iz).type());
        }
      }
    }

    events.fire(new WorldGeneratedEvent());
  }
}
