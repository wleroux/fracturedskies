package fs.math;

import org.junit.Assert;
import org.junit.Test;

import static fs.math.Matrix4.mat4;
import static fs.math.Vector3.vec3;

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

    private static void assertEquals(Matrix4 expected, Matrix4 actual) {
        Assert.assertEquals("m00", expected.m00(), actual.m00(), 0.00001f);
        Assert.assertEquals("m01", expected.m01(), actual.m01(), 0.00001f);
        Assert.assertEquals("m02", expected.m02(), actual.m02(), 0.00001f);
        Assert.assertEquals("m03", expected.m03(), actual.m03(), 0.00001f);
        Assert.assertEquals("m10", expected.m10(), actual.m10(), 0.00001f);
        Assert.assertEquals("m11", expected.m11(), actual.m11(), 0.00001f);
        Assert.assertEquals("m12", expected.m12(), actual.m12(), 0.00001f);
        Assert.assertEquals("m13", expected.m13(), actual.m13(), 0.00001f);
        Assert.assertEquals("m20", expected.m20(), actual.m20(), 0.00001f);
        Assert.assertEquals("m21", expected.m21(), actual.m21(), 0.00001f);
        Assert.assertEquals("m22", expected.m22(), actual.m22(), 0.00001f);
        Assert.assertEquals("m23", expected.m23(), actual.m23(), 0.00001f);
        Assert.assertEquals("m30", expected.m30(), actual.m30(), 0.00001f);
        Assert.assertEquals("m31", expected.m31(), actual.m31(), 0.00001f);
        Assert.assertEquals("m32", expected.m32(), actual.m32(), 0.00001f);
        Assert.assertEquals("m33", expected.m33(), actual.m33(), 0.00001f);
    }
}
