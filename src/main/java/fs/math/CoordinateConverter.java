package fs.math;

import fs.client.ui.game.Location;
import fs.client.world.Direction;

import java.util.ArrayList;
import java.util.List;

public final class CoordinateConverter {

  private final int width;
  private final int height;
  private final int depth;

  public CoordinateConverter(int width, int height, int depth) {
    this.width = width;
    this.height = height;
    this.depth = depth;
  }

  public int index(int x, int y, int z) {
    return z * width * height + y * width + x;
  }

  public int x(int index) {
    return index % width;
  }

  public int y(int index) {
    return (index / width) % height;
  }

  public int z(int index) {
    return (index / width / height);
  }

  public List<Location> neighbours(Location location) {
    List<Location> neighbours = new ArrayList<>();
    for (Direction direction: Direction.values()) {
      Location neighbour = direction.neighbour(location);
      if (neighbour.isWithinWorldLimits()) {
        neighbours.add(neighbour);
      }
    }
    return neighbours;
  }
}
