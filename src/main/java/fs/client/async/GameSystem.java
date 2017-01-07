package fs.client.async;

import java.util.concurrent.CompletableFuture;

/**
 * A game system
 */
public interface GameSystem {

    /**
     * Checks if the provided event can be received by this game system
     *
     * @param event  the event
     * @return  TRUE if this game system can handle the event; FALSE otherwise
     */
    boolean canHandle(Object event);

    /**
     * Handles the provided event
     * @param event  the event
     * @param future  the future indicating that this work is complete
     */
    void accept(Object event, CompletableFuture<Void> future);
}
