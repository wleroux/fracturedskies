package fs.math;

import org.junit.Test;

import static fs.math.Matrix4.mat4;
import static fs.math.Vector3.vec3;
import static org.junit.Assert.assertEquals;

public class Matrix4Test {
    private static final Matrix4 IDENTITY = mat4(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
    );

    @Test
    public void itInverts() {
        Matrix4 m1 = mat4(vec3(1, 2, 3));
        Matrix4 m2 = mat4(vec3(1, 2, 3)).invert();

        assertEquals(IDENTITY, m1.multiply(m2));
    }
}
