package fs.client.ui.event;

public interface EventHandler<T extends UIEvent> {
  void handle(T event);
}
