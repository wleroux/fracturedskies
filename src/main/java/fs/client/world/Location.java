package fs.client.world;

public class Location {
  private final World world;
  private final int x;
  private final int y;
  private final int z;

  public Location(World world, int x, int y, int z) {
    this.world = world;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public BlockState block() {
    return world.block(x, y, z);
  }

  public World world() {
    return world;
  }

  public int x() {
    return x;
  }

  public int y() {
    return y;
  }

  public int z() {
    return z;
  }

  public int hashCode() {
    return world.hashCode() + Integer.hashCode(x) + Integer.hashCode(y) + Integer.hashCode(z);
  }

  public boolean equals(Object object) {
    if (!(object instanceof Location)) {
      return false;
    }

    Location other = (Location) object;
    return other.world == this.world && other.x == this.x && other.y == this.y && other.z == this.z;
  }

  public int index() {
    return world.converter().index(x, y, z);
  }

  public boolean isWithinWorldLimits() {
    if (0 > x() || x() >= world.width()) {
      return false;
    }
    if (0 > y() || y() >= world.height()) {
      return false;
    }
    if (0 > z() || z() >= world.depth()) {
      return false;
    }
    return true;
  }
}
