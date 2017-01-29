package fs.client.system.world;

import fs.client.async.Dispatcher;
import fs.client.async.GameSystem;
import fs.client.event.Initialized;
import fs.client.event.WorldGenerated;
import fs.client.world.Tile;
import fs.client.world.World;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class WorldGenerationSystem implements GameSystem {


    private static final int SEED = new Random().nextInt();
    private static final int WORLD_WIDTH = 4;
    private static final int WORLD_HEIGHT = WORLD_WIDTH;
    private static final int WORLD_DEPTH = WORLD_WIDTH;
    private final Dispatcher dispatcher;

    public WorldGenerationSystem(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

//    private static final WorldGenerator worldGenerator = new WorldGenerator(WORLD_WIDTH, WORLD_HEIGHT, WORLD_DEPTH);


    @Override
    public boolean canHandle(Object event) {
        return event instanceof Initialized;
    }

    @Override
    public void accept(Object event, CompletableFuture<Void> future) {
        World world = new World(WORLD_WIDTH, WORLD_HEIGHT, WORLD_DEPTH);
        for (int ix = 0; ix < WORLD_WIDTH; ix ++) {
            for (int iz = 0; iz < WORLD_DEPTH; iz ++) {
                world.setBlock(world.converter().index(ix, 0, iz), Tile.BLOCK);
            }

            world.setBlock(world.converter().index(ix, 1, 1), Tile.BLOCK);
        }

        world.setBlock(world.converter().index(1, 0, 0), null);
        world.setBlock(world.converter().index(1, 0, 1), null);
        world.setBlock(world.converter().index(1, 0, 2), null);

        dispatcher.dispatch(new WorldGenerated(world), future);
    }
}
