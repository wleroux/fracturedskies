package fs.client.async;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.CompletableFuture.allOf;

/**
 * The Dispatcher ensures that an event is propagated to all game systems.
 *
 * This is a multi-threaded game engine; each system cannot communicate directly with other game states. If state must
 * be communicated, a message should be dispatched for other systems to handle the event.
 */
public final class Dispatcher {

    private final Set<GameSystem> systems = new HashSet<>();
    private final Map<GameSystem, ExecutorService> executors = new HashMap<>();

    public CompletableFuture<Void> dispatch(final Object event) {
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        dispatch(event, future);
        return future;
    }

    /**
     * Dispatches an event to all game systems. The provided future will be completed when all game systems have
     * processed the event.
     *
     * @param event  the event
     * @param future  the completable future
     */
    public void dispatch(final Object event, CompletableFuture<Void> future) {
        Set<CompletableFuture<Void>> systemFutures = new HashSet<>();
        for (GameSystem system: systems) {
            if (!system.canHandle(event)) {
                continue;
            }

            ExecutorService executor = executors.computeIfAbsent(system, (s) ->
                Executors.newSingleThreadExecutor(new DaemonThreadFactory("system-" + s.toString()))
            );

            CompletableFuture<Void> systemFuture = new CompletableFuture<>();
            systemFutures.add(systemFuture);
            executor.submit(() -> {
                try {
                    system.accept(event, systemFuture);
                } catch (Throwable ex) {
                    systemFuture.completeExceptionally(ex);
                }
            });
        }

        allOf(systemFutures.toArray(new CompletableFuture[0]))
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        future.completeExceptionally(failure);
                    } else {
                        future.complete(null);
                    }
                });
    }

    public void register(GameSystem system) {
        systems.add(system);
    }
}
