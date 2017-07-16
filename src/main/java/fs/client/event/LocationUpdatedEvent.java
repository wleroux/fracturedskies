package fs.client.event;

import fs.client.world.Location;

public class LocationUpdatedEvent {
  private final Location location;

  public LocationUpdatedEvent(Location location) {
    this.location = location;
  }

  public Location location() {
    return location;
  }
}
