package fs.math;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class CoordinateConverterTest {

    private static final int WIDTH = 5;
    private static final int HEIGHT = 5;
    private static final int DEPTH = 5;

    private static final CoordinateConverter converter = new CoordinateConverter(WIDTH, HEIGHT, DEPTH);

    @Test
    public void itConvertsCoordinates() {
        Random r = new Random();
        for (int i = 0; i < 100; i ++) {
            int x = Math.abs(r.nextInt()) % WIDTH;
            int y = Math.abs(r.nextInt()) % HEIGHT;
            int z = Math.abs(r.nextInt()) % DEPTH;

            int index = converter.index(x, y, z);
            assertEquals("index", index, z * WIDTH * HEIGHT + y * WIDTH + x);
            assertEquals("x", x, converter.x(index));
            assertEquals("y", y, converter.y(index));
            assertEquals("z", z, converter.z(index));
        }
    }
}
