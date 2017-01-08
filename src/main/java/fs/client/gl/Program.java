package fs.client.gl;

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
