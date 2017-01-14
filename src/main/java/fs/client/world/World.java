package fs.client.world;

public class World {

    private final int width;
    private final int height;
    private final int depth;

    private final int[] blocks;

    public World(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        blocks = new int[width * height * depth];
    }

    public int get(int x, int y, int z) {
        return blocks[index(x, y, z)];
    }

    public World set(int x, int y, int z, int block) {
        blocks[index(x, y, z)] = block;

        return this;
    }

    public int index(int x, int y, int z) {
        return z * width * height + y * width + x;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int depth() {
        return depth;
    }

}
