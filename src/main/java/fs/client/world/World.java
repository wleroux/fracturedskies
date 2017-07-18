package fs.client.world;

import fs.math.CoordinateConverter;

public class World {

  public static final int MAX_WATER_LEVEL = 8;
  public static final Location INVALID_LOCATION = new Location(null, -1, -1, -1) {
    @Override
    public boolean isWithinWorldLimits() {
      return false;
    }
  };

  private final CoordinateConverter converter;
  private final int width;
  private final int height;
  private final int depth;
  private final BlockState[] blocks;
  private final Location[] locations;

  public World(int width, int height, int depth) {
    this.width = width;
    this.height = height;
    this.depth = depth;

    converter = new CoordinateConverter(width, height, depth);

    blocks = new BlockState[width * height * depth];
    locations = new Location[width * height * depth];
    for (int i = 0; i < blocks.length; i ++) {
      blocks[i] = new BlockState(BlockType.AIR, 0);
      locations[i] = new Location(this, converter.x(i), converter.y(i), converter.z(i));
    }
  }

  public Location location(int x, int y, int z) {
    if (0 > x || x >= width()) {
      return INVALID_LOCATION;
    }
    if (0 > y || y >= height()) {
      return INVALID_LOCATION;
    }
    if (0 > z || z >= depth()) {
      return INVALID_LOCATION;
    }

    return locations[converter.index(x, y, z)];
  }

  public Location[] locations() {
    return locations;
  }

  public BlockState block(int x, int y, int z) {
    return blocks[converter.index(x, y, z)];
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  public int depth() {
    return depth;
  }

  public int size() {
    return width * height * depth;
  }

  public CoordinateConverter converter() {
    return converter;
  }
}
