package fs.client.world;

import java.util.Objects;

public class Location {
  private final World world;
  private final int x;
  private final int y;
  private final int z;

  private Location[] neighbours;
  private final BlockState block;
  private final int index;

  public Location(World world, int x, int y, int z) {
    this.world = world;
    this.x = x;
    this.y = y;
    this.z = z;
    this.block = world != null ? world.block(x, y, z) : null;
    this.index = world != null ? world.converter().index(x, y, z) : -1;
  }

  public BlockState block() {
    return block;
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
    return Objects.hash(x, y, z);
  }

  public boolean equals(Object object) {
    if (!(object instanceof Location)) {
      return false;
    }

    Location other = (Location) object;
    return other.world == this.world && other.x == this.x && other.y == this.y && other.z == this.z;
  }

  public int index() {
    return index;
  }

  public boolean isWithinWorldLimits() {
    return true;
  }

  public Location neighbour(Direction direction) {
    return neighbours()[direction.ordinal()];
  }

  private static Direction[] directions = Direction.values();
  public Location[] neighbours() {
    if (neighbours == null) {
      neighbours = new Location[6];
      for (Direction direction: directions) {
        neighbours[direction.ordinal()] = world.location(x + direction.dx(), y + direction.dy(), z + direction.dz());
      }
    }
    return neighbours;
  }
}
