package fs.render.ui.primitive.label;

import fs.render.ui.primitive.OpenGLComponent;
import fs.render.ui.primitive.mesh.Mesh;
import fs.render.ui.primitive.mesh.Program;
import fs.render.ui.primitive.mesh.TextureArray;
import fs.util.math.Color4;
import fs.util.math.Matrix4;

import static fs.util.math.Vector3.vec3;
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
  private int leftMargin = 0;
  private int rightMargin = 0;
  private int topMargin = 0;
  private int bottomMargin = 0;

  // derived fields
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

  public String text() {
    return text;
  }

  public Label text(String text) {
    this.text = text;
    return this;
  }

  public Label color(Color4 color) {
    this.color.set(color);
    return this;
  }

  public Label margin(int top, int right, int bottom, int left) {
    this.topMargin = top;
    this.rightMargin = right;
    this.bottomMargin = bottom;
    this.leftMargin = left;

    return this;
  }

  @Override
  public Label bounds(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;

    return this;
  }

  @Override
  public int preferredWidth() {
    return LabelMeshGenerator.width(text) + leftMargin + rightMargin;
  }

  @Override
  public int preferredHeight() {
    return LabelMeshGenerator.height(text) + topMargin + bottomMargin;
  }

  public void render() {
    if (!displayText().isEmpty()) {
      mesh = LabelMeshGenerator.generate(displayText());

      Matrix4 model = Matrix4.mat4(vec3(x + leftMargin, y + topMargin, 0));

      glUseProgram(program.id());
      uniform(COLOR_LOCATION, color);
      uniform(MODEL_LOCATION, model);
      uniform(PROJECTION_LOCATION, projection);
      uniform(ALBEDO_LOCATION, GL_TEXTURE0, textureArray);
      draw(mesh);

      glUseProgram(0);
    }
  }

  public String displayText() {
    return LabelMeshGenerator.displayText(text, width, height);
  }

  public int cursorPositionAt(int x, int y) {
    int relativeX = x - (this.x + this.leftMargin);
    int characterWidth = LabelMeshGenerator.width(".");
    return Math.max(0, Math.min((relativeX + characterWidth / 2) / characterWidth, text().length()));
  }
}
