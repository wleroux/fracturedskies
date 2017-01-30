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

    private Matrix4 projection;

    private String text = "";
    private Color4 color = Color4.color(0f, 0f, 0f, 1f);
    private int leftPadding = 0;
    private int rightPadding = 0;
    private int topPadding = 0;
    private int bottomPadding = 0;

    private Mesh mesh;

    public Label(Matrix4 projection) {
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

        this.projection = projection;
    }

    public Label text(String text) {
        this.text = text;
        return this;
    }

    public Label color(Color4 color) {
        this.color.set(color);
        return this;
    }

    public Label padding(int top, int right, int bottom, int left) {
        this.topPadding = top;
        this.rightPadding = right;
        this.bottomPadding = bottom;
        this.leftPadding = left;

        return this;
    }

    @Override
    public int preferredWidth() {
        return LabelMeshGenerator.width(text) + leftPadding + rightPadding;
    }

    @Override
    public int preferredHeight() {
        return LabelMeshGenerator.height(text) + topPadding + bottomPadding;
    }

    public void render(int xOffset, int yOffset, int width, int height) {
        if (!text.isEmpty()) {
            String displayText = LabelMeshGenerator.displayText(text, width, height);
            mesh = LabelMeshGenerator.generate(displayText);

            Matrix4 model = Matrix4.mat4(vec3(xOffset + leftPadding, yOffset + topPadding, -1));

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
