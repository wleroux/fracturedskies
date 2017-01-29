package fs.client.ui.primitive.mesh;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Program {
    private final int id;

    public Program(String vertexShaderSource, String fragmentShaderSource) {
        id = glCreateProgram();
        int vertexShaderId = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShaderId = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);

        glAttachShader(id, vertexShaderId);
        glAttachShader(id, fragmentShaderId);
        glLinkProgram(id);

        int success = glGetProgrami(id, GL_LINK_STATUS);
        if (success != GL_TRUE) {
            String errorMessage = glGetProgramInfoLog(id);
            throw new RuntimeException("Could not link program: " + errorMessage);
        }

        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
    }

    private static int compileShader(int type, String source) {
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);

        int success = glGetShaderi(shaderId, GL_COMPILE_STATUS);
        if (success != GL_TRUE) {
            String errorMessage = glGetShaderInfoLog(shaderId);
            throw new RuntimeException("Could not compile shader: " + errorMessage + "\n" + source);
        }

        return shaderId;
    }

    public int id() {
        return id;
    }
}
