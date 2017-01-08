package fs.math;

import static java.lang.String.format;

public class Vector3 {
    private float x;
    private float y;
    private float z;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public Vector3 x(float x) {
        this.x = x;
        return this;
    }

    public Vector3 y(float y) {
        this.y = y;
        return this;
    }

    public Vector3 z(float z) {
        this.z = z;
        return this;
    }

    public Vector3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;

        return this;
    }

    public String toString() {
        return format("[%d %d %d]", x, y, z);
    }

    public static Vector3 vec3(float x, float y, float z) {
        return new Vector3(x, y, z);
    }
}
