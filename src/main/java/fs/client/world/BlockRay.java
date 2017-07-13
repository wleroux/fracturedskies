package fs.client.world;

import fs.math.Vector3;

import java.util.Optional;

import static fs.math.Vector3.vec3;

public class BlockRay {

  private final World world;
  private final Vector3 origin;
  private final Vector3 direction;
  private final Vector3[] normals;

  public BlockRay(World world, Vector3 origin, Vector3 direction) {
    this.world = world;
    this.origin = origin;
    this.direction = direction;
    this.normals = new Vector3[]{
        vec3(-direction.x(), 0, 0).normalize(),
        vec3(0, -direction.y(), 0).normalize(),
        vec3(0, 0, -direction.z()).normalize()
    };
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

        if (hit.location().block().type() == null) {
          continue;
        }

        minDistance = d;
        minBlockRay = Optional.of(hit);
      }
    }

    return minBlockRay;
  }
}
