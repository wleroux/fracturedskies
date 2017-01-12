package fs.math;

import org.junit.Test;

import static fs.math.Matrix4.mat4;
import static fs.math.Quaternion4.quat4;
import static fs.math.Vector3.vec3;
import static junit.framework.TestCase.assertEquals;

public class Quaternion4Test {
    private static final Vector3 AXIS_NONE =  vec3(0, 0, 0);
    private static final Vector3 AXIS_POS_X = vec3(+1,  0,  0);
    private static final Vector3 AXIS_POS_Y = vec3( 0, +1,  0);
    private static final Vector3 AXIS_POS_Z = vec3( 0,  0, +1);
    private static final Vector3 AXIS_NEG_X = vec3(-1,  0,  0);
    private static final Vector3 AXIS_NEG_Y = vec3( 0, -1,  0);
    private static final Vector3 AXIS_NEG_Z = vec3( 0,  0, -1);

    @Test
    public void itRotatesVertices() {
        Quaternion4 rotation = quat4(vec3(1, 0, 0), (float) Math.PI / 2f);

        assertEquals( AXIS_NONE,  vec3(AXIS_NONE).rotate(rotation));

        assertEquals(AXIS_POS_X, vec3(AXIS_POS_X).rotate(rotation));
        assertEquals(AXIS_NEG_X, vec3(AXIS_NEG_X).rotate(rotation));

        assertEquals(AXIS_POS_Z, vec3(AXIS_POS_Y).rotate(rotation));
        assertEquals(AXIS_NEG_Y, vec3(AXIS_POS_Z).rotate(rotation));
        assertEquals(AXIS_NEG_Z, vec3(AXIS_NEG_Y).rotate(rotation));
        assertEquals(AXIS_POS_Y, vec3(AXIS_NEG_Z).rotate(rotation));
    }

    @Test
    public void itRotatesAsMatrices() {
        Quaternion4 rotation = quat4(vec3(1, 0, 0), (float) Math.PI / 2f);

        assertEquals( AXIS_NONE,  vec3(AXIS_NONE).multiply(mat4(rotation)));

        assertEquals(AXIS_POS_X, vec3(AXIS_POS_X).multiply(mat4(rotation)));
        assertEquals(AXIS_NEG_X, vec3(AXIS_NEG_X).multiply(mat4(rotation)));

        assertEquals(AXIS_POS_Z, vec3(AXIS_POS_Y).multiply(mat4(rotation)));
        assertEquals(AXIS_NEG_Y, vec3(AXIS_POS_Z).multiply(mat4(rotation)));
        assertEquals(AXIS_NEG_Z, vec3(AXIS_NEG_Y).multiply(mat4(rotation)));
        assertEquals(AXIS_POS_Y, vec3(AXIS_NEG_Z).multiply(mat4(rotation)));
    }


    @Test
    public void itConjugates() {
        Quaternion4 rotation = quat4(vec3(1, 0, 0), (float) Math.PI / 2f);
        Quaternion4 negRotation = quat4(rotation).conjugate();

        assertEquals( AXIS_NONE,  vec3(AXIS_NONE).rotate(rotation).rotate(negRotation));

        assertEquals(AXIS_POS_X, vec3(AXIS_POS_X).rotate(rotation).rotate(negRotation));
        assertEquals(AXIS_NEG_X, vec3(AXIS_NEG_X).rotate(rotation).rotate(negRotation));

        assertEquals(AXIS_POS_Y, vec3(AXIS_POS_Y).rotate(rotation).rotate(negRotation));
        assertEquals(AXIS_POS_Z, vec3(AXIS_POS_Z).rotate(rotation).rotate(negRotation));
        assertEquals(AXIS_NEG_Y, vec3(AXIS_NEG_Y).rotate(rotation).rotate(negRotation));
        assertEquals(AXIS_NEG_Z, vec3(AXIS_NEG_Z).rotate(rotation).rotate(negRotation));
    }
}
