package fs.render.event;

public interface EventHandler<T extends UIEvent> {
  void handle(T event);
}
