package fs.client.world;

import fs.math.CoordinateConverter;

public class World {

  public static final int MAX_WATER_LEVEL = 4;
  private final CoordinateConverter converter;
  private final int width;
  private final int height;
  private final int depth;
  private final Tile[] blocks;
  private final int[] waterLevel;

  public World(int width, int height, int depth) {
    this.width = width;
    this.height = height;
    this.depth = depth;
    blocks = new Tile[width * height * depth];
    waterLevel = new int[width * height * depth];
    converter = new CoordinateConverter(width, height, depth);
  }

  public World(World world) {
    this(world.width(), world.height(), world.depth());
    for (int i = 0; i < world.blocks.length; i++) {
      this.blocks[i] = world.blocks[i];
      this.waterLevel[i] = world.waterLevel[i];
    }
  }

  public Tile getBlock(int index) {
    return blocks[index];
  }

  public World setBlock(int index, Tile tile) {
    blocks[index] = tile;

    return this;
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

  public int[] waterLevel() {
    return waterLevel;
  }

  public void waterLevel(int[] waterLevel) {
    for (int i = 0; i < waterLevel.length; i++) {
      this.waterLevel[i] = waterLevel[i];
    }
  }

  public int size() {
    return width * height * depth;
  }

  public int waterLevel(int index) {
    return waterLevel[index];
  }

  public void waterLevel(int index, int waterLevel) {
    this.waterLevel[index] = waterLevel;
  }

  public CoordinateConverter converter() {
    return converter;
  }
}
