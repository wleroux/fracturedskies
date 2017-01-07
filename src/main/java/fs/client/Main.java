package fs.client;

import fs.client.async.Dispatcher;
import fs.client.async.GameSystem;
import fs.client.event.*;
import fs.client.system.RenderSystem;

import java.util.concurrent.CompletableFuture;

public class Main implements Runnable, GameSystem {
    private final Dispatcher dispatcher;
    private boolean isTerminated = false;

    public static void main(String[] args) {
        new Main().run();
    }

    public Main() {
        this.dispatcher = new Dispatcher();
        this.dispatcher.register(this);
        this.dispatcher.register(new RenderSystem(dispatcher));
    }

    @Override
    public void run() {
        CompletableFuture<Void> initializedFuture = new CompletableFuture<>();
        dispatcher.dispatch(new Initialized(), initializedFuture);
        initializedFuture.join();

        while (!isTerminated) {
            CompletableFuture<Void> updateRequestedFuture = new CompletableFuture<>();
            dispatcher.dispatch(new UpdateRequested(), updateRequestedFuture);
            updateRequestedFuture.join();

            CompletableFuture<Void> renderRequestedFuture = new CompletableFuture<>();
            dispatcher.dispatch(new RenderRequested(), renderRequestedFuture);
            renderRequestedFuture.join();
        }

        CompletableFuture<Void> terminatedRequestedFuture = new CompletableFuture<>();
        dispatcher.dispatch(new Terminated(), terminatedRequestedFuture);
        terminatedRequestedFuture.join();
    }

    public boolean canHandle(Object event) {
        return event instanceof TerminationRequested;
    }

    @Override
    public void accept(Object event, CompletableFuture<Void> future) {
        if (event instanceof TerminationRequested) {
            isTerminated = true;
        }
        future.complete(null);
    }

    @Override
    public String toString() {
        return "main";
    }
}
