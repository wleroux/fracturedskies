package fs.client.ui.primitive.mesh;

import fs.client.ui.primitive.OpenGLComponent;
import fs.math.Matrix4;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class MeshRenderer extends OpenGLComponent {

    private Mesh mesh;
    private Program program;
    private TextureArray textureArray;
    private Matrix4 model;
    private Matrix4 view;
    private Matrix4 projection;

    public MeshRenderer(Mesh mesh, Program program, TextureArray textureArray, Matrix4 model, Matrix4 view, Matrix4 projection) {
        this.mesh = mesh;
        this.program = program;
        this.textureArray = textureArray;
        this.model = model;
        this.view = view;
        this.projection = projection;
    }

    @Override
    public int preferredWidth() {
        return 0;
    }

    @Override
    public int preferredHeight() {
        return 0;
    }

    public void render(int xOffset, int yOffset, int width, int height) {
        glUseProgram(program.id());
        uniform(MODEL_LOCATION, model);
        uniform(VIEW_LOCATION, view);
        uniform(PROJECTION_LOCATION, projection);
        uniform(ALBEDO_LOCATION, GL_TEXTURE0, textureArray);
        draw(mesh);
        glUseProgram(0);
    }
}