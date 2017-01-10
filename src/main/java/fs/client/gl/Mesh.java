package fs.client.gl;

import org.omg.CORBA.FloatHolder;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL41.*;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.opengl.GL45.*;

public class Mesh {

    private final int vao;
    private final int indexCount;

    private static final int POSITION_LOCATION = 0;
    private static final int TEXCOORD_LOCATION = 1;
    private static final int NORMAL_LOCATION = 2;

    public Mesh(float[] vertices, int[] indices) {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(POSITION_LOCATION, 3, GL_FLOAT, false, 9 * Float.BYTES, 0 * Float.BYTES);
        glEnableVertexAttribArray(POSITION_LOCATION);

        glVertexAttribPointer(TEXCOORD_LOCATION, 3, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(TEXCOORD_LOCATION);

        glVertexAttribPointer(NORMAL_LOCATION, 3, GL_FLOAT, false, 9 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(NORMAL_LOCATION);

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        indexCount = indices.length;

        glBindVertexArray(0);
    }

    public int vao() {
        return vao;
    }

    public int indexCount() {
        return indexCount;
    }
}
