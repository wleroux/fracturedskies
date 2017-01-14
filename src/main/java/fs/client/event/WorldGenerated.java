package fs.client.event;

import fs.client.world.World;

public class WorldGenerated {
    private final World world;

    public WorldGenerated(World world) {
        this.world = world;
    }

    public World world() {
        return world;
    }
}
