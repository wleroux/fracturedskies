package fs.event;

import fs.world.Location;
import fs.block.BlockType;

public final class BlockAddedEvent extends LocationUpdatedEvent implements Cancellable {
  private final BlockType block;
  private boolean cancelled = false;

  public BlockAddedEvent(Location location, BlockType block) {
    super(location);
    this.block = block;
  }

  public BlockType type() {
    return block;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }
}