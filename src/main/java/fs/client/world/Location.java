package fs.client.world;

import java.util.*;

public class Location {
  private final World world;
  private final int x;
  private final int y;
  private final int z;

  private Map<Direction, Location> neighbours;
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
    int result = 1;
    result = 31 * result + x;
    result = 31 * result + y;
    result = 31 * result + z;

    return result;
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
    return neighbours().getOrDefault(direction, World.INVALID_LOCATION);
  }

  private static Direction[] directions = Direction.values();
  public Map<Direction, Location> neighbours() {
    if (neighbours == null) {
      neighbours = new HashMap<>();
      for (Direction direction: directions) {
        Location location = world.location(x + direction.dx(), y + direction.dy(), z + direction.dz());
        if (location.isWithinWorldLimits()) {
          neighbours.put(direction, world.location(x + direction.dx(), y + direction.dy(), z + direction.dz()));
        }
      }
    }
    return neighbours;
  }
}
