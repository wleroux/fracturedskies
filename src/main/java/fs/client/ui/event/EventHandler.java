package fs.client.ui.event;

public interface EventHandler<T extends Event> {
    void handle(T event);
}
