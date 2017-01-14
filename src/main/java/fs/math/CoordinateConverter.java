package fs.math;

import java.util.ArrayList;
import java.util.List;

public final class CoordinateConverter {

    private final int width;
    private final int height;
    private final int depth;

    public CoordinateConverter(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public int index(int x, int y, int z) {
        return z * width * height + y * width + x;
    }

    public int x(int index) {
        return index % width;
    }

    public int y(int index) {
        return (index / width) % height;
    }

    public int z(int index) {
        return (index / width / height);
    }

    public List<Integer> neighbours(int index) {
        List<Integer> neighbours = new ArrayList<>();
        if (x(index) != 0)
            neighbours.add(index - 1);
        if (x(index) + 1 != width)
            neighbours.add(index + 1);

        if (z(index) != 0)
            neighbours.add(index - width * height);
        if (z(index) + 1 != depth)
            neighbours.add(index + width * height);

        if (y(index) != 0)
            neighbours.add(index - width);
        if (y(index) + 1 != height)
            neighbours.add(index + width);
        return neighbours;
    }
}
