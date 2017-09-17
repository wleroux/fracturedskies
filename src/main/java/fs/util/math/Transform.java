package fs.util.math;

import static fs.util.math.Matrix4.mat4;
import static fs.util.math.Quaternion4.quat4;
import static fs.util.math.Vector3.vec3;

public final class Transform {

  private final Vector3 position = vec3();
  private final Quaternion4 rotation = quat4();


  public Transform(Vector3 position, Quaternion4 rotation) {
    this.position.set(position);
    this.rotation.set(rotation);
  }

  public Transform() {
    this(vec3(), quat4());
  }

  public Matrix4 matrix4() {
    return mat4(position, rotation);
  }

  public Vector3 position() {
    return position;
  }

  public Quaternion4 rotation() {
    return rotation;
  }
}
