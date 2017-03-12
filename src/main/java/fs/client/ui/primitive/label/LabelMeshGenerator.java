package fs.client.ui.primitive.label;

import fs.client.ui.primitive.mesh.Mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static fs.client.ui.primitive.OpenGLComponent.*;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.BufferUtils.createIntBuffer;

public class LabelMeshGenerator {

    private static final int CHAR_WIDTH = 8;
    private static final int CHAR_HEIGHT = 9;

    public static int width(String text) {
        byte[] characters = text.getBytes(US_ASCII);
        return characters.length * 8;
    }

    public static int height(String text) {
        return CHAR_HEIGHT;
    }

    public static Mesh generate(String text) {
        byte[] characters = text.getBytes(US_ASCII);
        FloatBuffer verticesBuffer = createFloatBuffer(6 * 4 * characters.length);
        IntBuffer indicesBuffer = createIntBuffer(6 * characters.length);
        int vertexCount = 0;
        for (int charIndex = 0; charIndex < characters.length; charIndex ++) {
            verticesBuffer.put(new float[] {
                    (charIndex + 0) * CHAR_WIDTH, CHAR_HEIGHT, 0,  0, 0, characters[charIndex],
                    (charIndex + 1) * CHAR_WIDTH, CHAR_HEIGHT, 0,  1, 0, characters[charIndex],
                    (charIndex + 1) * CHAR_WIDTH,           0, 0,  1, 1, characters[charIndex],
                    (charIndex + 0) * CHAR_WIDTH,           0, 0,  0, 1, characters[charIndex]
            });
            indicesBuffer.put(new int[] {
                    vertexCount + 0, vertexCount + 1, vertexCount + 2,
                    vertexCount + 2, vertexCount + 3, vertexCount + 0
            });
            vertexCount += 4;
        }

        verticesBuffer.flip();
        float[] vertices = new float[verticesBuffer.remaining()];
        verticesBuffer.get(vertices);

        indicesBuffer.flip();
        int[] indices = new int[indicesBuffer.remaining()];
        indicesBuffer.get(indices);

        return new Mesh(vertices, indices, POSITION, TEXCOORD);
    }

    public static String displayText(String text, int width, int height) {
        int characters = width / CHAR_WIDTH;
        if (characters >= text.length()) {
            return text;
        } else if (characters < 3) {
            return "";
        } else {
            return text.substring(0, characters - 3) + "...";
        }
    }
}
