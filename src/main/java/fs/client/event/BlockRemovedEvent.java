package fs.client.event;

import fs.client.ui.game.Location;

public final class BlockRemovedEvent extends BlockUpdatedEvent implements Cancellable {
  private final Location location;
  private boolean cancelled;

  public BlockRemovedEvent(Location location) {
    this.location = location;
  }

  public Location location() {
    return location;
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
