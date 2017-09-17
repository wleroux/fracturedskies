package fs.world.water;

import fs.event.game.TickEvent;
import fs.event.WaterUpdatedEvent;
import fs.world.Location;
import fs.world.World;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Random;

@Singleton
public class EvaporationSystem {
  private static final int EVAPORTATION_INTERVAL = 8;

  private final Random random = new Random();

  @Inject
  private World world;

  @Inject
  private Event<Object> events;

  private int tick = 0;

  private static float PROBABILITY_OF_EVAPORATION = 0.01f;

  public void evaporate(@Observes TickEvent event) {
    tick ++;
    if (tick % EVAPORTATION_INTERVAL != 0)
      return;

    int w = world.width(), h = world.height(), d = world.depth();
    for (int iz = 0; iz < d; iz ++) {
      for (int iy = 0; iy < h; iy++) {
        for (int ix = 0; ix < w; ix++) {
          if (random.nextFloat() <= PROBABILITY_OF_EVAPORATION) {
            Location location = world.location(ix, iy, iz);
            if (location.block().waterLevel() == 1) {
              location.block().waterLevel(0);
              events.fire(new WaterUpdatedEvent(location));
            }
          }
        }
      }
    }
  }
}
