package fs.client.ui.primitive;

import fs.client.ui.Component;
import fs.client.ui.primitive.mesh.Mesh;
import fs.client.ui.primitive.mesh.TextureArray;
import fs.math.Color4;
import fs.math.Matrix4;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static fs.client.ui.primitive.mesh.Mesh.Attribute.attribute;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public abstract class OpenGLComponent extends Component {
  /**
   * vertex attributes
   */
  public static final int POSITION_LOCATION = 0;
  public static final int TEXCOORD_LOCATION = 1;
  public static final int NORMAL_LOCATION = 2;

  public static final Mesh.Attribute POSITION = attribute(POSITION_LOCATION, GL_FLOAT, 3, Float.BYTES);
  public static final Mesh.Attribute TEXCOORD = attribute(TEXCOORD_LOCATION, GL_FLOAT, 3, Float.BYTES);
  public static final Mesh.Attribute NORMAL = attribute(NORMAL_LOCATION, GL_FLOAT, 3, Float.BYTES);

  /**
   * uniforms
   */
  public static final int MODEL_LOCATION = 0;
  public static final int VIEW_LOCATION = 1;
  public static final int PROJECTION_LOCATION = 2;
  public static final int ALBEDO_LOCATION = 3;
  public static final int COLOR_LOCATION = 4;

  private static final FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
  private static final FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(4);

  protected static void draw(Mesh mesh) {
    glBindVertexArray(mesh.vao());
    glDrawElements(GL_TRIANGLES, mesh.indexCount(), GL_UNSIGNED_INT, 0);
  }

  protected static void uniform(int location, int texture, TextureArray textureArray) {
    glUniform1i(location, texture - GL_TEXTURE0);
    glActiveTexture(texture);
    glBindTexture(GL_TEXTURE_2D_ARRAY, textureArray.id());
  }

  protected static void uniform(int location, Color4 color) {
    color.store(vec4Buffer);
    vec4Buffer.flip();
    glUniform4fv(location, vec4Buffer);
  }

  protected static void uniform(int location, Matrix4 mat4) {
    mat4.store(mat4Buffer);
    mat4Buffer.flip();
    glUniformMatrix4fv(location, false, mat4Buffer);
  }
}
