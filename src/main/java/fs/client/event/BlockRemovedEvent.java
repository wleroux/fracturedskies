package fs.client.event;

public final class BlockRemovedEvent extends BlockUpdatedEvent implements Cancellable {
  private final int index;
  private boolean cancelled;

  public BlockRemovedEvent(int index) {
    this.index = index;
  }

  public int index() {
    return index;
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
