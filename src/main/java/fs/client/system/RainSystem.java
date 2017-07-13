package fs.client.system;

import fs.client.event.BlockUpdatedEvent;
import fs.client.event.TickEvent;
import fs.client.world.World;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RainSystem {

  private static final int RAIN_INTERVAL = 15;

  @Inject
  private Event<Object> events;

  @Inject
  private World world;

  private int tick = 0;

  public void onUpdateRequested(@Observes TickEvent event) {
    tick++;
    if (tick % RAIN_INTERVAL == 0) {
      world.block(
          world.width() / 2,
          world.height() - 1,
          world.depth() / 2
      ).waterLevel(1);
      events.fire(new BlockUpdatedEvent());
    }
  }
}
