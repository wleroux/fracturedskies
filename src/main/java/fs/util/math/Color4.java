package fs.util.math;

import java.nio.FloatBuffer;

public class Color4 {

  private float r;
  private float g;
  private float b;
  private float a;

  public Color4(float r, float g, float b, float a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }

  public static Color4 color(float r, float g, float b, float a) {
    return new Color4(r, g, b, a);
  }

  public Color4 r(float r) {
    this.r = r;
    return this;
  }

  public float r() {
    return r;
  }

  public Color4 g(float g) {
    this.g = g;
    return this;
  }

  public float g() {
    return g;
  }

  public Color4 b(float b) {
    this.b = b;
    return this;
  }

  public float b() {
    return b;
  }

  public Color4 a(float a) {
    this.a = a;
    return this;
  }

  public float a() {
    return a;
  }

  public Color4 set(Color4 color) {
    this.r = color.r;
    this.g = color.g;
    this.b = color.b;
    this.a = color.a;

    return this;
  }

  public void store(FloatBuffer buffer) {
    buffer.put(r);
    buffer.put(g);
    buffer.put(b);
    buffer.put(a);
  }
}
