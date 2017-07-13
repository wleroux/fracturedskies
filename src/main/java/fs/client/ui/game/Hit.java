package fs.client.ui.game;

import fs.math.Vector3;

public class Hit {

  private final Location location;
  private final Vector3 intersection;
  private final Vector3 normal;

  public Hit(Location location, Vector3 intersection, Vector3 normal) {
    this.location = location;
    this.intersection = intersection;
    this.normal = normal;
  }

  public Location location() {
    return location;
  }

  public Vector3 intersection() {
    return intersection;
  }

  public Vector3 normal() {
    return normal;
  }
}
