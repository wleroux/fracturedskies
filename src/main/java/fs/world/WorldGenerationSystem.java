package fs.world;

import fs.Game;
import fs.event.game.GameInitializationEvent;
import fs.event.WorldGeneratedEvent;

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
    worldGenerator.generate(world, SEED);
    events.fire(new WorldGeneratedEvent());
  }
}
