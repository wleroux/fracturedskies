package fs.client.ui.game;

import fs.client.async.Dispatcher;
import fs.client.async.RemoveBlockRequested;
import fs.client.ui.Component;
import fs.client.ui.event.Event;
import fs.client.ui.event.MouseDown;
import fs.client.ui.primitive.mesh.Mesh;
import fs.client.ui.primitive.mesh.MeshRenderer;
import fs.client.ui.primitive.mesh.Program;
import fs.client.ui.primitive.mesh.TextureArray;
import fs.client.world.WaterMeshGenerator;
import fs.client.world.World;
import fs.client.world.WorldMeshGenerator;
import fs.math.Matrix4;
import fs.math.Quaternion4;
import fs.math.Vector3;
import fs.math.Vector4;

import static fs.client.system.world.WorldGenerationSystem.*;
import static fs.math.Matrix4.mat4;
import static fs.math.Matrix4.orthogonal;
import static fs.math.Quaternion4.quat4;
import static fs.math.Vector3.vec3;
import static fs.math.Vector4.vec4;
import static fs.util.ResourceLoader.loadAsByteBuffer;
import static fs.util.ResourceLoader.loadAsString;

public class WorldRenderer extends Component {

    private final Dispatcher dispatcher;

    private final Program program;
    private final TextureArray textureArray;

    private final Matrix4 projection = mat4();
    private final Matrix4 inverseProjection = mat4();

    private final Vector3 viewPosition = vec3();
    private final Matrix4 view = mat4();
    private final Matrix4 inverseView = mat4();

    private final Vector3 modelPosition = vec3();
    private final Matrix4 model = mat4();

    private World world;
    private int tickCount = 0;
    private int screenWidth;
    private int screenHeight;

    private Mesh blockMesh;
    private MeshRenderer blockRenderer;

    private Mesh waterMesh;
    private MeshRenderer waterRenderer;

    public WorldRenderer(Dispatcher dispatcher, int screenWidth, int screenHeight) {
        this.dispatcher = dispatcher;

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

        projection(orthogonal(-10, 10, -10, 10, 0.03f, 1000f));
    }

    private WorldRenderer projection(Matrix4 mat4) {
        projection.set(mat4);
        inverseProjection.set(mat4).invert();

        return this;
    }

    public void tickCount(int tickCount) {
        this.tickCount = tickCount;
    }

    private WorldRenderer view(Vector3 position, Quaternion4 rotation) {
        viewPosition.set(position);
        inverseView.set(mat4(viewPosition, rotation));
        view.set(inverseView).invert();

        return this;
    }

    public void world(World world) {
        this.world = world;

        model(vec3(-(world.width() / 2), 0f, -(world.height() / 2)));
        view(vec3(0.0f, 12f, -3f * world.depth()), quat4(vec3(1, 0, 0), (float) Math.PI / 4f));

        // Draw world
        dirty();
    }

    private WorldRenderer model(Vector3 position) {
        modelPosition.set(position);
        model.set(mat4(position));

        return this;
    }

    private void dirty() {
        blockMesh = WorldMeshGenerator.generateMesh(world);
        blockRenderer = null;

        waterMesh = WaterMeshGenerator.generateMesh(world);
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

    public void handle(Event event) {
        if (event instanceof MouseDown) {
            int mx = ((MouseDown) event).x();
            int my = ((MouseDown) event).y();
            int index = pick(mx, my);
            if (index != -1) {
                dispatcher.dispatch(new RemoveBlockRequested(index));
            }
        }
    }

    private int pick(int mx, int my) {
        Vector4 rayStart = vec4(
                -((float) (screenWidth - mx) / (float) screenWidth - 0.5f) * 2,
                ((float) my / (float) screenHeight - 0.5f) * 2,
                -1f,
                1f
        )
                .multiply(inverseProjection)
                .multiply(inverseView);
        rayStart.multiply(1f / rayStart.w());

        Vector4 rayEnd = vec4(
                -((float) (screenWidth - mx) / (float) screenWidth - 0.5f) * 2,
                ((float) (screenHeight - my) / (float) screenHeight - 0.5f) * 2,
                1f,
                1f
        )
                .multiply(inverseProjection)
                .multiply(inverseView);
        rayEnd.multiply(1f / rayEnd.w());

        Vector4 rayVector = vec4(rayEnd).subtract(rayStart);
        int steps = (int) rayVector.magnitude() * 4;
        rayVector.normalize().multiply(1f/4f);

        Vector3 stepVector = vec3(rayVector.x(), rayVector.y(), rayVector.z());
        Vector3 testPoint = vec3(rayStart.x(), rayStart.y(), rayStart.z())
                                .subtract(modelPosition);
        for (int i = 0; i < steps; i ++) {
            float x = testPoint.x() + 0.5f;
            float y = testPoint.y() + 0.5f;
            float z = testPoint.z() + 0.5f;

            if (0 <= x && x < WORLD_WIDTH && 0 <= y && y < WORLD_HEIGHT && 0 <= z && z < WORLD_DEPTH) {
                int index = world.converter().index((int) x, (int) y, (int) z);
                if (world.getBlock(index) != null) {
                    return index;
                }
            }

            testPoint.add(stepVector);
        }

        return -1;
    }
}
