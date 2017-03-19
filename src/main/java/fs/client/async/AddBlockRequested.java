package fs.client.async;

import fs.client.world.Tile;

public class AddBlockRequested {
    private final int index;
    private final Tile tile;

    public AddBlockRequested(int index, Tile tile) {
        this.index = index;
        this.tile = tile;
    }

    public int index() {
        return index;
    }

    public Tile tile() {
        return tile;
    }
}
