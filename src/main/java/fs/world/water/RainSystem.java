package fs.world.water;

import fs.event.game.TickEvent;
import fs.event.WaterUpdatedEvent;
import fs.world.Location;
import fs.world.World;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import static fs.world.World.MAX_WATER_LEVEL;

@Singleton
public class RainSystem {

  private static final int RAIN_INTERVAL = 1;

  @Inject
  private Event<Object> events;

  @Inject
  private World world;

  private int tick = 0;

  public void onUpdateRequested(@Observes TickEvent event) {
    tick++;
    if (tick % RAIN_INTERVAL == 0) {
      Location rainLocation = world.location(
          world.width() / 2,
          world.height() - 1,
          world.depth() / 2
      );
      if (rainLocation.block().waterLevel() != MAX_WATER_LEVEL) {
        rainLocation.block().waterLevel(MAX_WATER_LEVEL);
        events.fire(new WaterUpdatedEvent(rainLocation));
      }
    }
  }
}