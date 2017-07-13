package fs.client.world;

import fs.client.world.Location;
import fs.client.world.World;
import fs.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class BlockRayHit {

  private final World world;
  private final Vector3 intersection;
  private final Vector3 normal;
  private final Vector3 direction;
  private Location location;

  public BlockRayHit(World world, Vector3 intersection, Vector3 direction, Vector3 normal) {
    this.world = world;
    this.intersection = intersection;
    this.direction = direction;
    this.normal = normal;
  }

  public Location location() {
    if (location == null) {
      int blockX = blockCoordinate(intersection.x(), normal.x());
      int blockY = blockCoordinate(intersection.y(), normal.y());
      int blockZ = blockCoordinate(intersection.z(), normal.z());
      location = new Location(world, blockX, blockY, blockZ);
    }
    return location;
  }

   private static int blockCoordinate(float coordinate, float normal) {
    if (coordinate % 1 == 0 && normal > 0) {
      return (int) coordinate - 1;
    } else {
      return (int) Math.floor(coordinate);
    }
  }

  public Vector3 intersection() {
    return intersection;
  }

  public Vector3 normal() {
    return normal;
  }

  public Vector3 direction() {
    return direction;
  }

  public List<Direction> faces() {
    List<Direction> faces = new ArrayList<>();
    if (this.normal.x() != 0) {
      faces.add(this.normal.x() > 0 ? Direction.EAST : Direction.WEST);
    }
    if (this.normal.y() != 0) {
      faces.add(this.normal.y() > 0 ? Direction.UP : Direction.DOWN);
    }
    if (this.normal.z() != 0) {
      faces.add(this.normal.z() > 0 ? Direction.SOUTH : Direction.NORTH);
    }

    return faces;
  }
}
