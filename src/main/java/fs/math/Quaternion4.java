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
    ).normalize();
  }

  public Quaternion4 normalize() {
    float m = magnitude();
    return set(
      w / m,
      x / m,
      y / m,
      z / m
    );
  }

  public float magnitude() {
    return (float) Math.sqrt(w * w + x * x + y * y + z * z);
  }

  public static Quaternion4 quat4() {
    return quat4(1, 0, 0, 0);
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
    this.x = x;
    return this;
  }

  public float y() {
    return y;
  }

  public Quaternion4 y(float y) {
    this.y = y;
    return this;
  }

  public float z() {
    return z;
  }

  public Quaternion4 z(float z) {
    this.z = z;
    return this;
  }

  public Quaternion4 set(float w, float x, float y, float z) {
    this.w = w;
    this.x = x;
    this.y = y;
    this.z = z;

    return this;
  }

  public Quaternion4 set(Quaternion4 o) {
    return set(o.w(), o.x(), o.y(), o.z());
  }

  public String toString() {
    return format("[%f %f %f %f]", w, x, y, z);
  }

  public Quaternion4 conjugate() {
    return set(w, -x, -y, -z);
  }

  public Quaternion4 multiply(float s) {
    float halfAngle = (float) Math.acos(w);
    float sin = (float) Math.sin(halfAngle);
    float ax = x / sin;
    float ay = y / sin;
    float az = z / sin;

    float newHalfAngle = halfAngle * s;
    float newSin = (float) Math.sin(newHalfAngle);
    return set(
        (float) Math.cos(newHalfAngle),
        ax * newSin,
        ay * newSin,
        az * newSin
    );
  }

  public Quaternion4 multiply(Quaternion4 o) {
    return set(
      w * o.w - x * o.x - y * o.y - z * o.z,
      w * o.x + x * o.w - y * o.z + z * o.y,
      w * o.y + x * o.z + y * o.w - z * o.x,
      w * o.z - x * o.y + y * o.x + z * o.w
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Quaternion4)) {
      return false;
    }

    Quaternion4 q = (Quaternion4) o;

    return Math.abs(w - q.w) <= 0.00001f &&
        Math.abs(x - q.x) <= 0.00001f &&
        Math.abs(y - q.y) <= 0.00001f &&
        Math.abs(z - q.z) <= 0.00001f;
  }
}
