package fs.client.world;

import fs.math.Vector3;

import java.util.Optional;
import java.util.function.Predicate;

import static fs.math.Vector3.vec3;

public class BlockRay {

  public static final Predicate<BlockRayHit> FILTER_AIR = (hit) ->
    hit.location().block().type() == BlockType.AIR;
  public static final Predicate<BlockRayHit> FILTER_NONE = (hit) -> false;


  private final World world;
  private final Vector3 origin;
  private final Vector3 direction;
  private final Vector3[] normals;
  private final Predicate<BlockRayHit> skipFilter;
  private final Predicate<BlockRayHit> stopFilter;

  public BlockRay(World world, Vector3 origin, Vector3 direction, Predicate<BlockRayHit> skipFilter, Predicate<BlockRayHit> stopFilter) {
    this.world = world;
    this.origin = origin;
    this.direction = direction;
    this.normals = new Vector3[]{
        vec3(-direction.x(), 0, 0).normalize(),
        vec3(0, -direction.y(), 0).normalize(),
        vec3(0, 0, -direction.z()).normalize()
    };
    this.skipFilter = skipFilter;
    this.stopFilter = stopFilter;
  }

  public Optional<BlockRayHit> get() {
    float minDistance = Float.MAX_VALUE;
    Optional<BlockRayHit> minBlockRay = Optional.empty();
    for (Vector3 normal : normals) {
      if (normal.magnitude() == 0)
        continue;

      float ON = origin.dot(normal);
      float DN = direction.dot(normal);

      for (int plane = -world.width(); plane < world.width(); plane++) {
        // distance = -(origin dot normal + plane) / (direction dot normal)
        float d = -(ON + plane) / DN;
        if (d > minDistance) {
          continue;
        }

        // intersection = origin + distance * direction
        Vector3 intersection = vec3(direction).multiply(d).add(origin);
        BlockRayHit hit = new BlockRayHit(world, intersection, direction, normal);
        if (!hit.location().isWithinWorldLimits()) {
          continue;
        }

        if (stopFilter.test(hit)) {
          return minBlockRay;
        }

        if (skipFilter.test(hit)) {
          continue;
        }

        minDistance = d;
        minBlockRay = Optional.of(hit);
      }
    }

    return minBlockRay;
  }
}
