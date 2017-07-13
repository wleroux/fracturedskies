package fs.client.world;

import fs.math.CoordinateConverter;

public class World {

  public static final int MAX_WATER_LEVEL = 4;
  private final CoordinateConverter converter;
  private final int width;
  private final int height;
  private final int depth;
  private final BlockState[] blocks;

  public World(int width, int height, int depth) {
    this.width = width;
    this.height = height;
    this.depth = depth;
    blocks = new BlockState[width * height * depth];
    for (int i = 0; i < blocks.length; i ++) {
      blocks[i] = new BlockState(null, 0);
    }

    converter = new CoordinateConverter(width, height, depth);
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
