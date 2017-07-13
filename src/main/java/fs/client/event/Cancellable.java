package fs.client.event;

public interface Cancellable {
  boolean isCancelled();

  void setCancelled(boolean cancelled);
}
