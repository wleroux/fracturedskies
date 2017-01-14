package fs.client.system.render;

import fs.client.gl.Mesh;
import fs.client.gl.Program;
import fs.client.gl.TextureArray;
import fs.math.Matrix4;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * Created by FracturedSkies on 1/14/2017.
 */
public class MeshRenderer {

    private static final FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);

    public static void render(Mesh mesh, Program program, TextureArray textureArray, Matrix4 model, Matrix4 view, Matrix4 projection) {
        glUseProgram(program.id());

        model.store(modelBuffer);
        modelBuffer.flip();
        glUniformMatrix4fv(0, false, modelBuffer);

        view.store(viewBuffer);
        viewBuffer.flip();
        glUniformMatrix4fv(1, false, viewBuffer);

        projection.store(projectionBuffer);
        projectionBuffer.flip();
        glUniformMatrix4fv(2, false, projectionBuffer);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureArray.id());

        glBindVertexArray(mesh.vao());
        glDrawElements(GL_TRIANGLES, mesh.indexCount(), GL_UNSIGNED_INT, 0);
        glUseProgram(0);
    }
}
