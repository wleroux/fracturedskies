package fs.client.ui.primitive.label;

import fs.client.ui.primitive.OpenGLComponent;
import fs.client.ui.primitive.mesh.Mesh;
import fs.client.ui.primitive.mesh.Program;
import fs.client.ui.primitive.mesh.TextureArray;
import fs.math.Color4;
import fs.math.Matrix4;

import static fs.math.Vector3.vec3;
import static fs.util.ResourceLoader.loadAsByteBuffer;
import static fs.util.ResourceLoader.loadAsString;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Label extends OpenGLComponent {

    private final Program program;
    private final TextureArray textureArray;

    private String text;
    private Color4 color;
    private Matrix4 model;
    private Matrix4 projection;

    private Mesh mesh;

    public Label(String text, Color4 color, Matrix4 projection) {
        this.text = text;
        this.color = color;

        ClassLoader classLoader = this.getClass().getClassLoader();
        this.program = new Program(
                loadAsString("fs/client/ui/primitive/label/label.vs", classLoader),
                loadAsString("fs/client/ui/primitive/label/label.fs", classLoader)
        );
        this.textureArray = new TextureArray(
                loadAsByteBuffer("fs/client/ui/primitive/label/font.png", classLoader),
                8,
                9,
                128
        );

        this.model = model;
        this.projection = projection;
    }

    @Override
    public int preferredWidth() {
        return LabelMeshGenerator.width(text);
    }

    @Override
    public int preferredHeight() {
        return LabelMeshGenerator.height(text);
    }

    public void render(int xOffset, int yOffset, int width, int height) {
        if (!text.isEmpty()) {
            String displayText = LabelMeshGenerator.displayText(text, width, height);
            mesh = LabelMeshGenerator.generate(displayText);

            Matrix4 model = Matrix4.mat4(vec3(xOffset, yOffset, -1));

            glUseProgram(program.id());
            uniform(COLOR_LOCATION, color);
            uniform(MODEL_LOCATION, model);
            uniform(PROJECTION_LOCATION, projection);
            uniform(ALBEDO_LOCATION, GL_TEXTURE0, textureArray);
            draw(mesh);

            glUseProgram(0);
        }
    }
}
