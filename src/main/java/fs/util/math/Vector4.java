package fs.util.math;

import static java.lang.String.format;

public class Vector4 {
  private float x;
  private float y;
  private float z;
  private float w;

  public Vector4(float x, float y, float z, float w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public static Vector4 vec4() {
    return vec4(0, 0, 0, 0);
  }

  public static Vector4 vec4(float x, float y, float z, float w) {
    return new Vector4(x, y, z, w);
  }

  public static Vector4 vec4(Vector4 v) {
    return new Vector4(v.x, v.y, v.z, v.w);
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

  public float w() {
    return w;
  }

  public Vector4 x(float x) {
    this.x = x;
    return this;
  }

  public Vector4 y(float y) {
    this.y = y;
    return this;
  }

  public Vector4 z(float z) {
    this.z = z;
    return this;
  }

  public Vector4 w(float w) {
    this.w = w;
    return this;
  }

  public Vector4 set(float x, float y, float z, float w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;

    return this;
  }

  public Vector4 multiply(Matrix4 mat4) {
    return set(
        x * mat4.m00() + y * mat4.m01() + z * mat4.m02() + w * mat4.m03(),
        x * mat4.m10() + y * mat4.m11() + z * mat4.m12() + w * mat4.m13(),
        x * mat4.m20() + y * mat4.m21() + z * mat4.m22() + w * mat4.m23(),
        x * mat4.m30() + y * mat4.m31() + z * mat4.m32() + w * mat4.m33()
    );
  }

  public String toString() {
    return format("[%f %f %f %f]", x, y, z, w);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Vector4)) {
      return false;
    }

    Vector4 v = (Vector4) o;

    return Math.abs(x - v.x) <= 0.00001f &&
        Math.abs(y - v.y) <= 0.00001f &&
        Math.abs(z - v.z) <= 0.00001f &&
        Math.abs(w - v.w) <= 0.00001f;
  }

  public Vector4 subtract(Vector4 v) {
    return set(x - v.x, y - v.y, z - v.z, w - v.w);
  }

  public Vector4 set(Vector4 v) {
    return set(v.x, v.y, v.z, v.w);
  }

  public Vector4 normalize() {
    return multiply(1 / magnitude());
  }

  public Vector4 multiply(float c) {
    return set(x * c, y * c, z * c, w * c);
  }

  public float magnitude() {
    return (float) Math.sqrt(x * x + y * y + z * z + w * w);
  }

  public Vector4 add(Vector4 v) {
    return set(x + v.x, y + v.y, z + v.z, w + v.w);
  }
}
