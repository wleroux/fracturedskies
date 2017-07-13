package fs.client.world;

import fs.math.Vector3;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static fs.math.Vector3.vec3;

public class BlockRay implements Iterator<BlockRayHit> {

  public static final Predicate<BlockRayHit> FILTER_AIR = filterBlockType(BlockType.AIR);
  public static final Predicate<BlockRayHit> FILTER_NONE = (hit) -> false;
  private static final Predicate<BlockRayHit> FILTER_OUTSIDE_WORLD_LIMITS = (hit) -> !hit.location().isWithinWorldLimits();
  private BlockRayHit previewNext;

  public static Predicate<BlockRayHit> filterBlockType(final BlockType blockType) {
    return (hit) -> hit.location().block().type() == blockType;
  }

  public static Predicate<BlockRayHit> filterWithinRadius(Vector3 origin, float distance) {
    return (hit) -> vec3(hit.intersection()).subtract(origin).magnitude() > distance;
  }

  private final World world;
  private final Vector3 direction;
  private final Vector3[] normals;
  private final Predicate<BlockRayHit> skipFilter;
  private final Predicate<BlockRayHit> stopFilter;

  private final Vector3 intersection;
  private final Vector3 travelVector = vec3();
  private final Vector3 normal = vec3();

  public BlockRay(World world, Vector3 origin, Vector3 direction, Predicate<BlockRayHit> skipFilter, Predicate<BlockRayHit> stopFilter) {
    this.world = world;
    this.intersection = vec3(origin);
    this.direction = direction;
    this.normals = new Vector3[]{
        vec3(-direction.x(), 0, 0).normalize(),
        vec3(0, -direction.y(), 0).normalize(),
        vec3(0, 0, -direction.z()).normalize()
    };
    this.skipFilter = FILTER_OUTSIDE_WORLD_LIMITS.or(skipFilter);
    this.stopFilter = stopFilter;
  }

  public BlockRayHit next() {
    if (previewNext != null) {
      BlockRayHit next = previewNext;
      previewNext = null;
      return next;
    }

    while (true) {
      float nextPlaneX = nextPlane(intersection.x(), direction.x(), 0, world.width());
      float timeX = direction.x() != 0 ? (nextPlaneX - intersection.x()) / direction.x() : Float.MAX_VALUE;

      float nextPlaneY = nextPlane(intersection.y(), direction.y(), 0, world.height());
      float timeY = direction.y() != 0 ? (nextPlaneY - intersection.y()) / direction.y() : Float.MAX_VALUE;

      float nextPlaneZ = nextPlane(intersection.z(), direction.z(), 0, world.depth());
      float timeZ = direction.z() != 0 ? (nextPlaneZ - intersection.z()) / direction.z() : Float.MAX_VALUE;

      float minimumTime = Math.min(Math.min(timeX, timeY), timeZ);

      // Force planes that are out-of-bounds to intersected
      if (0 > intersection.x() && direction.x() > 0 || intersection.x() > world.width() && direction.x() < 0)
        minimumTime = Math.max(minimumTime, timeX);
      if (0 > intersection.y() && direction.z() > 0 || intersection.y() > world.height() && direction.y() < 0)
        minimumTime = Math.max(minimumTime, timeY);
      if (0 > intersection.z() && direction.z() > 0 || intersection.z() > world.depth() && direction.z() < 0)
        minimumTime = Math.max(minimumTime, timeZ);

      // Any further amount of time would lead us out-of-bounds
      if (minimumTime <= 0) {
        throw new NoSuchElementException();
      }

      travelVector.set(direction).multiply(minimumTime);
      intersection.add(travelVector);

      normal.set(0f, 0f, 0f);
      if (timeX == minimumTime)
        normal.add(normals[0]);
      if (timeY == minimumTime)
        normal.add(normals[1]);
      if (timeZ == minimumTime)
        normal.add(normals[2]);
      normal.normalize();

      BlockRayHit hit = new BlockRayHit(world, intersection, direction, normal);
      if (stopFilter.test(hit)) {
        throw new NoSuchElementException();
      }

      if (skipFilter.test(hit)) {
        continue;
      }

      return hit;
    }
  }

  private float nextPlane(float current, float direction, float minValue, float maxValue) {
    float floor = (float) Math.floor(current);
    if (direction > 0f) {
      return Math.min(floor + 1, maxValue);
    } else if (current != floor) {
      return Math.max(minValue, Math.min(floor, maxValue));
    } else {
      return Math.max(minValue, floor - 1);
    }
  }

  @Override
  public boolean hasNext() {
    try {
      this.previewNext = next();
    } catch (NoSuchElementException e) {
      return false;
    }
    return true;
  }
}
