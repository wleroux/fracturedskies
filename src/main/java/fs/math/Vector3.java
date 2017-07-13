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

  public static Vector3 vec3() {
    return vec3(0, 0, 0);
  }

  public static Vector3 vec3(float x, float y, float z) {
    return new Vector3(x, y, z);
  }

  public static Vector3 vec3(Vector3 v) {
    return new Vector3(v.x, v.y, v.z);
  }

  public static Vector3 vec3(Vector4 v) {
    return vec3(v.x(), v.y(), v.z());
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

  public Vector3 rotate(Quaternion4 quat) {

    float x2 = quat.x() * 2f;
    float y2 = quat.y() * 2f;
    float z2 = quat.z() * 2f;
    float xx2 = quat.x() * x2;
    float xy2 = quat.x() * y2;
    float xz2 = quat.x() * z2;
    float xw2 = quat.w() * x2;
    float yy2 = quat.y() * y2;
    float yz2 = quat.y() * z2;
    float yw2 = quat.w() * y2;
    float zz2 = quat.z() * z2;
    float zw2 = quat.w() * z2;

    return set(
        (1f - (yy2 + zz2)) * x + (xy2 - zw2) * y + (xz2 + yw2) * z,
        (xy2 + zw2) * x + (1f - (xx2 + zz2)) * y + (yz2 - xw2) * z,
        (xz2 - yw2) * x + (yz2 + xw2) * y + (1f - (xx2 + yy2)) * z
    );
  }

  public Vector3 multiply(Matrix4 mat4) {
    return set(
        x * mat4.m00() + y * mat4.m01() + z * mat4.m02() + mat4.m03(),
        x * mat4.m10() + y * mat4.m11() + z * mat4.m12() + mat4.m13(),
        x * mat4.m20() + y * mat4.m21() + z * mat4.m22() + mat4.m23()
    );
  }

  public String toString() {
    return format("[%f %f %f]", x, y, z);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Vector3)) {
      return false;
    }

    Vector3 v = (Vector3) o;

    return Math.abs(x - v.x) <= 0.00001f &&
        Math.abs(y - v.y) <= 0.00001f &&
        Math.abs(z - v.z) <= 0.00001f;
  }

  public Vector3 subtract(Vector3 v) {
    return set(x - v.x, y - v.y, z - v.z);
  }

  public Vector3 set(Vector3 v) {
    return set(v.x, v.y, v.z);
  }

  public Vector3 normalize() {
    return multiply(1 / magnitude());
  }

  public Vector3 multiply(float c) {
    return set(x * c, y * c, z * c);
  }

  public float magnitude() {
    return (float) Math.sqrt(x * x + y * y + z * z);
  }

  public Vector3 add(Vector3 v) {
    return set(x + v.x, y + v.y, z + v.z);
  }

  public float dot(Vector3 v) {
    return x * v.x + y * v.y + z * v.z;
  }

  public Vector3 negate() {
    return multiply(-1);
  }
}
