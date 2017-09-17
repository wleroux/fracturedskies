package fs.render.ui.primitive.mesh;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.glTexSubImage3D;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL42.glTexStorage3D;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;


public final class TextureArray {

  private final int id;

  public TextureArray(ByteBuffer rawImageBuffer, int width, int height, int layers) {
    id = glGenTextures();
    glBindTexture(GL_TEXTURE_2D_ARRAY, id);

    ByteBuffer glImageBuffer = stbi_load_from_memory(rawImageBuffer, new int[]{width}, new int[]{height * layers}, new int[]{4}, 4);

    glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, 4, width, height, layers);
    glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, 0, width, height, layers, GL_RGBA, GL_UNSIGNED_BYTE, glImageBuffer);

    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
  }

  public int id() {
    return id;
  }
}
