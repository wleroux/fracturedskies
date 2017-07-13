package fs.client.world;

public enum Direction {
  NORTH(0, 0, -1),
  EAST(1, 0, 0),
  SOUTH(0, 0, 1),
  WEST(-1, 0, 0),
  UP(0, 1, 0),
  DOWN(0, -1, 0);

  private final int dx;
  private final int dy;
  private final int dz;

  Direction(int dx, int dy, int dz) {
    this.dx = dx;
    this.dy = dy;
    this.dz = dz;
  }

  public Location neighbour(Location location) {
    return new Location(location.world(), location.x() + dx, location.y() + dy, location.z() + dz);
  }
}
