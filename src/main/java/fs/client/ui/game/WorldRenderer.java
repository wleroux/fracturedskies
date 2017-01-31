package fs.client.ui.game;

import fs.client.ui.Component;
import fs.client.ui.primitive.mesh.Mesh;
import fs.client.ui.primitive.mesh.MeshRenderer;
import fs.client.ui.primitive.mesh.Program;
import fs.client.ui.primitive.mesh.TextureArray;
import fs.client.world.WaterMeshGenerator;
import fs.client.world.World;
import fs.client.world.WorldMeshGenerator;
import fs.math.Matrix4;
import fs.math.Quaternion4;

import static fs.math.Matrix4.mat4;
import static fs.math.Matrix4.perspective;
import static fs.math.Quaternion4.quat4;
import static fs.math.Vector3.vec3;
import static fs.util.ResourceLoader.loadAsByteBuffer;
import static fs.util.ResourceLoader.loadAsString;

public class WorldRenderer extends Component {

    private final Program program;
    private final TextureArray textureArray;
    private final Matrix4 projection;
    private MeshRenderer blockRenderer;
    private MeshRenderer waterRenderer;

    private Matrix4 model;
    private Mesh blockMesh;
    private Mesh waterMesh;
    private int tickCount = 0;
    private Matrix4 view;
    private World world;
    private int screenWidth;
    private int screenHeight;
    private int x;
    private int y;
    private int width;
    private int height;

    public WorldRenderer(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        ClassLoader classLoader = this.getClass().getClassLoader();
        program = new Program(
                loadAsString("fs/client/ui/primitive/mesh/default.vs", classLoader),
                loadAsString("fs/client/ui/primitive/mesh/default.fs", classLoader)
        );

        textureArray = new TextureArray(
                loadAsByteBuffer("fs/client/ui/primitive/mesh/tileset.png", classLoader),
                16,
                16,
                3
        );

        projection = perspective((float) Math.PI / 4, screenWidth, screenHeight, 0.03f, 1000f);
    }

    public void tickCount(int tickCount) {
        this.tickCount = tickCount;

        if (world != null) {
            Quaternion4 rotation = quat4(vec3(0, 1, 0), (float) Math.PI * ((float) tickCount / 360f));
            view = mat4(vec3(0.0f, 2f, -3f * world.depth()).rotate(rotation), rotation).invert();
            dirty();
        }
    }

    public void setWorld(World world) {
        this.world = world;
        model = mat4(vec3(-(world.width() / 2), 0f, -(world.height() / 2)));

        // Draw world
        blockMesh = WorldMeshGenerator.generateMesh(world);
        waterMesh = WaterMeshGenerator.generateMesh(world);
        dirty();
    }

    private void dirty() {
        blockRenderer = null;
        waterRenderer = null;
    }

    @Override
    public int preferredWidth() {
        return screenWidth;
    }

    @Override
    public int preferredHeight() {
        return screenHeight;
    }

    @Override
    public WorldRenderer bounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        return this;
    }

    @Override
    public void render() {
        if (world != null) {
            if (blockRenderer == null)
                blockRenderer = new MeshRenderer(blockMesh, program, textureArray, model, view, projection);
            blockRenderer
                    .bounds(x, y, width, height)
                    .render();

            if (waterRenderer == null)
                waterRenderer = new MeshRenderer(waterMesh, program, textureArray, model, view, projection);
            waterRenderer
                    .bounds(x, y, width, height)
                    .render();
        }
    }

    @Override
    public Component findComponentAt(int x, int y) {
        if (this.x <= x && x <= this.x + this.width) {
            if (this.y <= y && y <= this.y + this.height) {
                return this;
            }
        }
        return null;
    }

    public void update() {
        tickCount(tickCount + 1);
    }
}
