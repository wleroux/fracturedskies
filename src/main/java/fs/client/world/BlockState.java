package fs.client.world;

/**
 * The state of a type
 */
public class BlockState {
  private BlockType blockType;
  private int waterLevel = 0;

  public BlockState(BlockType blockType, int waterLevel) {
    this.blockType = blockType;
    this.waterLevel = waterLevel;
  }

  public int waterLevel() {
    return waterLevel;
  }

  public void waterLevel(int waterLevel) {
    this.waterLevel = waterLevel;
  }

  public BlockType type() {
    return blockType;
  }

  public void type(BlockType block) {
    this.blockType = block;
  }
}
