package fs.client.world;

import static fs.math.Interpolators.lerp;
import static fs.math.Interpolators.map;
import static fs.math.Noise.perlin;

public class WorldGenerator {

  private final int width;
  private final int height;
  private final int depth;

  public WorldGenerator(int width, int height, int depth) {
    this.width = width;
    this.height = height;
    this.depth = depth;
  }

  public World generate(int seed) {
    World world = new World(width, height, depth);

    for (int iy = 0; iy < height; iy++) {
      for (int ix = 0; ix < width; ix++) {
        for (int iz = 0; iz < depth; iz++) {
          float fx = map(ix, 0, width, -1, 1);
          float fy = map(iy, 0, height, 0, 1);
          float fz = map(iz, 0, depth, -1, 1);

          if (isBlock(seed, fx, fy, fz)) {
            world.block(ix, iy, iz).type(BlockType.BLOCK);
          } else {
            world.block(ix, iy, iz).type(BlockType.AIR);
          }
        }
      }
    }

    return world;
  }

  private boolean isBlock(int seed, float x, float y, float z) {
    float distFromCenterSquared = (x * x + z * z);

    float plateau = map(y, 0.9f, 1f, 1f, 0f);
    float cone = lerp(1, 0, distFromCenterSquared) * map(y, 0, 0.85f, 0, 1);

    // Terrain
    float terrain = perlin("terrain".hashCode() + seed, x * 2f, y * 2f, z * 2f, 5, 0.7f);
    terrain *= cone * plateau;

    // Cave Systems
    float caves = perlin("caves".hashCode() + seed, x * 4f, y * 4f, z * 4f, 5, 0.5f);
    caves *= 1 + 0.45f * y;
    return (terrain > 0.15f) && (caves > 0.45);
  }
}
