package fs.math;

import static java.lang.String.format;

public class Quaternion4 {
  private float w;
  private float x;
  private float y;
  private float z;

  public Quaternion4(float w, float x, float y, float z) {
    this.w = w;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public static Quaternion4 quat4(float w, float x, float y, float z) {
    return new Quaternion4(w, x, y, z);
  }

  public static Quaternion4 quat4(Quaternion4 q) {
    return new Quaternion4(q.w, q.x, q.y, q.z);
  }

  public static Quaternion4 quat4(Vector3 axis, float angle) {
    float s = (float) Math.sin(angle / 2);
    return quat4(
        (float) Math.cos(angle / 2),
        axis.x() * s,
        axis.y() * s,
        axis.z() * s
    );
  }

  public float w() {
    return w;
  }

  public Quaternion4 w(float w) {
    this.w = w;
    return this;
  }

  public float x() {
    return x;
  }

  public Quaternion4 x(float x) {
    this.w = x;
    return this;
  }

  public float y() {
    return y;
  }

  public Quaternion4 y(float y) {
    this.w = y;
    return this;
  }

  public float z() {
    return z;
  }

  public Quaternion4 z(float z) {
    this.w = z;
    return this;
  }

  public Quaternion4 set(float w, float x, float y, float z) {
    this.w = w;
    this.x = x;
    this.y = y;
    this.z = z;

    return this;
  }

  public String toString() {
    return format("[%f %f %f %f]", w, x, y, z);
  }

  public Quaternion4 conjugate() {
    return set(w, -x, -y, -z);
  }
}
