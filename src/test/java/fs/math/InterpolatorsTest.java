package fs.math;

import org.junit.Test;

import static fs.math.Interpolators.map;
import static org.junit.Assert.assertEquals;

public class InterpolatorsTest {

    @Test
    public void itMapsValuesToNewRanges() {
        assertEquals(-1f, map(0, 0, 200, -1, 1), 0.0001f);
        assertEquals(0, map(100, 0, 200, -1, 1), 0.0001f);
        assertEquals(1f, map(200, 0, 200, -1, 1), 0.0001f);
        assertEquals(2f, map(100, 0, 200, -1, 5), 0.0001f);
        assertEquals(5f, map(200, 0, 200, -1, 5), 0.0001f);

        assertEquals(-1f, map(-100, 0, 200, -1, 5), 0.0001f);
        assertEquals(5f, map(250, 0, 200, -1, 5), 0.0001f);
    }
}
