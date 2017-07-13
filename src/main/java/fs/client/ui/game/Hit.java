package fs.client.ui.game;

import fs.math.Vector3;

public class Hit {

  private final int index;
  private final Vector3 intersection;
  private final Vector3 normal;

  public Hit(int index, Vector3 intersection, Vector3 normal) {
    this.index = index;
    this.intersection = intersection;
    this.normal = normal;
  }

  public int index() {
    return index;
  }

  public Vector3 intersection() {
    return intersection;
  }

  public Vector3 normal() {
    return normal;
  }
}
