package fs.client;

import fs.client.async.Dispatcher;
import fs.client.async.GameSystem;
import fs.client.event.*;
import fs.client.system.render.RenderSystem;
import fs.client.system.water.WaterSystem;
import fs.client.system.world.WorldGenerationSystem;

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
        this.dispatcher.register(new WaterSystem(dispatcher));
        this.dispatcher.register(new RenderSystem(dispatcher));
        this.dispatcher.register(new WorldGenerationSystem(dispatcher));
    }

    @Override
    public void run() {
        dispatcher.dispatch(new Initialized()).join();

        while (!isTerminated) {
            dispatcher.dispatch(new UpdateRequested()).join();
            dispatcher.dispatch(new RenderRequested()).join();
        }

        dispatcher.dispatch(new Terminated()).join();
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
