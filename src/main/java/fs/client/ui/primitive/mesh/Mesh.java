package fs.client.ui.primitive.mesh;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh {

    private final int vao;
    private final int indexCount;


    public Mesh(float[] vertices, int[] indices, Attribute... attributes) {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

       int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int stride = 0;
        for (Attribute attribute: attributes) {
            stride += attribute.elements() * attribute.elementSize();
        }

        int offset = 0;
        for (Attribute attribute: attributes) {
            glVertexAttribPointer(attribute.location(), attribute.elements(), attribute.elementType(), false, stride, offset);
            glEnableVertexAttribArray(attribute.location());
            offset += attribute.elements() * attribute.elementSize();
        }

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

    public static class Attribute {

        private final int location;
        private final int elementType;
        private final int elements;
        private final int elementSize;

        public Attribute(int location, int elementType, int elements, int elementSize) {
            this.location = location;
            this.elementType = elementType;
            this.elements = elements;
            this.elementSize = elementSize;
        }

        public int location() {
            return location;
        }

        public int elementType() {
            return elementType;
        }

        public int elements() {
            return elements;
        }

        public int elementSize() {
            return elementSize;
        }

        public static Attribute attribute(int location, int elementType, int elements, int elementSize) {
            return new Attribute(location, elementType, elements, elementSize);
        }
    }
}
