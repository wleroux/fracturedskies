package fs.client.ui.label;

import fs.client.gl.Mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static fs.client.gl.Mesh.Attribute.attribute;
import static fs.client.gl.Mesh.POSITION_LOCATION;
import static fs.client.gl.Mesh.TEXCOORD_LOCATION;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.BufferUtils.createIntBuffer;
import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class LabelMeshGenerator {
    public static Mesh generate(String text) {
        byte[] characters = text.getBytes(US_ASCII);
        FloatBuffer verticesBuffer = createFloatBuffer(6 * 4 * characters.length);
        IntBuffer indicesBuffer = createIntBuffer(6 * characters.length);
        int vertexCount = 0;
        for (int charIndex = 0; charIndex < characters.length; charIndex ++) {
            int width = 8;
            int height = 9;

            verticesBuffer.put(new float[] {
                    (charIndex + 0) * width, height, 0,  0, 0, characters[charIndex],
                    (charIndex + 1) * width, height, 0,  1, 0, characters[charIndex],
                    (charIndex + 1) * width,      0, 0,  1, 1, characters[charIndex],
                    (charIndex + 0) * width,      0, 0,  0, 1, characters[charIndex]
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

        return new Mesh(vertices, indices,
                attribute(POSITION_LOCATION, GL_FLOAT, 3, Float.BYTES),
                attribute(TEXCOORD_LOCATION, GL_FLOAT, 3, Float.BYTES)
        );
    }
}
