package fs.event;

import fs.world.Location;

public class LocationUpdatedEvent {
  private final Location location;

  public LocationUpdatedEvent(Location location) {
    this.location = location;
  }

  public Location location() {
    return location;
  }
}
