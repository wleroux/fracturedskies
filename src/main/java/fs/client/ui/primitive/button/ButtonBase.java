package fs.client.ui.primitive.button;

import fs.client.ui.primitive.OpenGLComponent;
import fs.client.ui.primitive.mesh.Mesh;
import fs.client.ui.primitive.mesh.Program;
import fs.client.ui.primitive.mesh.TextureArray;
import fs.math.Color4;
import fs.math.Matrix4;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static fs.math.Vector3.vec3;
import static fs.util.ResourceLoader.loadAsByteBuffer;
import static fs.util.ResourceLoader.loadAsString;
import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.BufferUtils.createIntBuffer;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class ButtonBase extends OpenGLComponent {

    private final Program program;
    private final TextureArray textureArray;
    private final Matrix4 projection;
    private Color4 color = Color4.color(1f, 1f, 1f, 1f);

    public ButtonBase(Matrix4 projection) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        this.program = new Program(
                loadAsString("fs/client/ui/primitive/button/button.vs", classLoader),
                loadAsString("fs/client/ui/primitive/button/button.fs", classLoader)
        );
        this.textureArray = new TextureArray(
                loadAsByteBuffer("fs/client/ui/primitive/button/button.png", classLoader),
                4,
                4,
                9
        );

        this.projection = projection;
    }

    public ButtonBase color(Color4 color) {
        this.color.set(color);

        return this;
    }

    @Override
    public int preferredWidth() {
        return LAYER_WIDTH*3;
    }

    @Override
    public int preferredHeight() {
        return LAYER_HEIGHT*3;
    }

    @Override
    public void render(int xOffset, int yOffset, int width, int height) {
        Mesh mesh = generate(width, height);

        Matrix4 model = Matrix4.mat4(vec3(xOffset, yOffset, -1));

        glUseProgram(program.id());
        glDisable(GL_DEPTH_TEST);

        uniform(COLOR_LOCATION, color);
        uniform(MODEL_LOCATION, model);
        uniform(PROJECTION_LOCATION, projection);
        uniform(ALBEDO_LOCATION, GL_TEXTURE0, textureArray);
        draw(mesh);

        glUseProgram(0);
    }

    private static final int LAYER_WIDTH = 4;
    private static final int LAYER_HEIGHT = 4;
    private static Mesh generate(int width, int height) {
        FloatBuffer verticesBuffer = createFloatBuffer(6 * 4 * 9);
        IntBuffer indicesBuffer = createIntBuffer(6 * 9);

        int fillWidth = width - 2 * LAYER_WIDTH;
        int fillHeight = height - 2 * LAYER_HEIGHT;

        // @formatter:off
        int vertexCount = 0;
        for (int i = 0; i < 3; i ++) {
            for (int j = 0; j < 3; j ++) {
                int tile = j * 3 + i;
                int xOffset = (i != 2) ? ((i == 0) ? 0 : LAYER_WIDTH) : LAYER_WIDTH + fillWidth;
                int yOffset = ((j != 2) ? ((j == 0) ? 0 : LAYER_HEIGHT) : LAYER_HEIGHT + fillHeight);
                int w = (i != 2) ? ((i == 0) ? LAYER_WIDTH : fillWidth) : LAYER_WIDTH;
                int h = (j != 2) ? ((j == 0) ? LAYER_HEIGHT : fillHeight) : LAYER_HEIGHT;

                verticesBuffer.put(new float[] {
                    xOffset + 0, yOffset + 0, 0,  0, 0, tile,
                    xOffset + w, yOffset + 0, 0,  1, 0, tile,
                    xOffset + w, yOffset + h, 0,  1, 1, tile,
                    xOffset + 0, yOffset + h, 0,  0, 1, tile
                });
                indicesBuffer.put(new int[] {
                        vertexCount + 0, vertexCount + 1, vertexCount + 2,
                        vertexCount + 2, vertexCount + 3, vertexCount + 0
                });
                vertexCount += 4;
            }
        }
        // @formatter:on

        verticesBuffer.flip();
        float[] vertices = new float[verticesBuffer.remaining()];
        verticesBuffer.get(vertices);

        indicesBuffer.flip();
        int[] indices = new int[indicesBuffer.remaining()];
        indicesBuffer.get(indices);

        return new Mesh(vertices, indices, POSITION, TEXCOORD);
    }
}
