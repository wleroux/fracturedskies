package fs.event;

import fs.world.Location;

public class WaterUpdatedEvent extends LocationUpdatedEvent {
  public WaterUpdatedEvent(Location location) {
    super(location);
  }
}
