package fs.client.event;

import fs.client.world.Location;

public final class BlockRemovedEvent extends LocationUpdatedEvent implements Cancellable {
  private boolean cancelled;

  public BlockRemovedEvent(Location location) {
    super(location);
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
