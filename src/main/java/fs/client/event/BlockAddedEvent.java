package fs.client.event;

import fs.client.world.Location;
import fs.client.world.BlockType;

public final class BlockAddedEvent extends BlockUpdatedEvent implements Cancellable {
  private final Location location;
  private final BlockType block;
  private boolean cancelled = false;

  public BlockAddedEvent(Location location, BlockType block) {
    this.location = location;
    this.block = block;
  }

  public Location location() {
    return location;
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
