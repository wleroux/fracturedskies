package fs.client.event;

import fs.client.world.Location;

public class WaterUpdatedEvent extends LocationUpdatedEvent {
  public WaterUpdatedEvent(Location location) {
    super(location);
  }
}
