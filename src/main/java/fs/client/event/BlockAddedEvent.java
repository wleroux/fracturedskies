package fs.client.event;

import fs.client.world.Tile;

public final class BlockAddedEvent extends BlockUpdatedEvent implements Cancellable {
  private final int index;
  private final Tile tile;
  private boolean cancelled = false;

  public BlockAddedEvent(int index, Tile tile) {
    this.index = index;
    this.tile = tile;
  }

  public int index() {
    return index;
  }

  public Tile tile() {
    return tile;
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
