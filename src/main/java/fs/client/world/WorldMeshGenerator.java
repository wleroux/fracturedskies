package fs.client.world;

import fs.client.ui.primitive.mesh.Mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static fs.client.ui.primitive.OpenGLComponent.*;
import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.BufferUtils.createIntBuffer;

public class WorldMeshGenerator {
    public static Mesh generateMesh(World world) {
        FloatBuffer verticesBuffer = createFloatBuffer(
                9 * 4 * 6 * world.width() * world.depth()
        );
        IntBuffer indicesBuffer = createIntBuffer(
                6 * 6 * world.width() * world.depth()
        );

        int vertexCount = 0;
        for (int iy = 0; iy < world.height(); iy ++) {
            for (int ix = 0; ix < world.width(); ix ++) {
                for (int iz = 0; iz < world.depth(); iz ++) {
                    int index = world.converter().index(ix, iy, iz);

                    Tile tile = world.getBlock(index);
                    if (tile == null) {
                        continue;
                    }

                    float xOffset = (float) ix;
                    float yOffset = (float) iy;
                    float zOffset = (float) iz;

                    // front
                    if (shouldRenderFront(world, ix, iy, iz)) {
                        int tileIndex = tile.front();
                        verticesBuffer.put(new float[]{
                                // @formatter:off
                                xOffset + -0.5f, yOffset +  0.5f, zOffset + -0.5f,  0f, 0f, tileIndex,   0f, -1f,  0f,
                                xOffset +  0.5f, yOffset +  0.5f, zOffset + -0.5f,  0f, 1f, tileIndex,   0f, -1f,  0f,
                                xOffset +  0.5f, yOffset + -0.5f, zOffset + -0.5f,  1f, 1f, tileIndex,   0f, -1f,  0f,
                                xOffset + -0.5f, yOffset + -0.5f, zOffset + -0.5f,  1f, 0f, tileIndex,   0f, -1f,  0f,
                                // @formatter:on
                        });

                        indicesBuffer.put(new int[]{
                                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                                vertexCount + 2, vertexCount + 3, vertexCount + 0
                        });
                        vertexCount += 4;
                    }


                    // top
                    if (shouldRenderTop(world, ix, iy, iz)) {
                        int tileIndex = tile.top();
                        verticesBuffer.put(new float[]{
                            // @formatter:off
                            xOffset + -0.5f, yOffset + 0.5f,  zOffset +  0.5f,   0f, 0f, tileIndex,   0f,  1f,  0f,
                            xOffset +  0.5f, yOffset + 0.5f,  zOffset +  0.5f,   0f, 1f, tileIndex,   0f,  1f,  0f,
                            xOffset +  0.5f, yOffset + 0.5f,  zOffset + -0.5f,   1f, 1f, tileIndex,   0f,  1f,  0f,
                            xOffset + -0.5f, yOffset + 0.5f,  zOffset + -0.5f,   1f, 0f, tileIndex,   0f,  1f,  0f,
                            // @formatter:on
                        });

                        indicesBuffer.put(new int[]{
                                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                                vertexCount + 2, vertexCount + 3, vertexCount + 0
                        });
                        vertexCount += 4;
                    }

                    // left
                    if (shouldRenderLeft(world, ix, iy, iz)) {
                        int tileIndex = tile.left();
                        verticesBuffer.put(new float[]{
                                // @formatter:off
                            xOffset + -0.5f, yOffset +  0.5f, zOffset +  0.5f,  0f, 0f, tileIndex,  -1f,  0f,  0f,
                            xOffset + -0.5f, yOffset +  0.5f, zOffset + -0.5f,  0f, 1f, tileIndex,  -1f,  0f,  0f,
                            xOffset + -0.5f, yOffset + -0.5f, zOffset + -0.5f,  1f, 1f, tileIndex,  -1f,  0f,  0f,
                            xOffset + -0.5f, yOffset + -0.5f, zOffset +  0.5f,  1f, 0f, tileIndex,  -1f,  0f,  0f,
                            // @formatter:on
                        });

                        indicesBuffer.put(new int[]{
                                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                                vertexCount + 2, vertexCount + 3, vertexCount + 0
                        });
                        vertexCount += 4;
                    }

                    // right
                    if (shouldRenderRight(world, ix, iy, iz)) {
                        int tileIndex = tile.right();
                        verticesBuffer.put(new float[]{
                            // @formatter:off
                            xOffset +  0.5f, yOffset +  0.5f, zOffset + -0.5f,  0f, 0f, tileIndex,   1f,  0f,  0f,
                            xOffset +  0.5f, yOffset +  0.5f, zOffset +  0.5f,  0f, 1f, tileIndex,   1f,  0f,  0f,
                            xOffset +  0.5f, yOffset + -0.5f, zOffset +  0.5f,  1f, 1f, tileIndex,   1f,  0f,  0f,
                            xOffset +  0.5f, yOffset + -0.5f, zOffset + -0.5f,  1f, 0f, tileIndex,   1f,  0f,  0f,
                            // @formatter:on
                        });

                        indicesBuffer.put(new int[]{
                                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                                vertexCount + 2, vertexCount + 3, vertexCount + 0
                        });
                        vertexCount += 4;
                    }

                    // bottom
                    if (shouldRenderBottom(world, ix, iy, iz)) {
                        int tileIndex = tile.bottom();
                        verticesBuffer.put(new float[]{
                            // @formatter:off
                            xOffset + -0.5f, yOffset + -0.5f, zOffset + -0.5f,   0f, 0f, tileIndex,  0f, -1f,  0f,
                            xOffset +  0.5f, yOffset + -0.5f, zOffset + -0.5f,   0f, 1f, tileIndex,  0f, -1f,  0f,
                            xOffset +  0.5f, yOffset + -0.5f, zOffset +  0.5f,   1f, 1f, tileIndex,  0f, -1f,  0f,
                            xOffset + -0.5f, yOffset + -0.5f, zOffset +  0.5f,   1f, 0f, tileIndex,  0f, -1f,  0f,
                            // @formatter:on
                        });

                        indicesBuffer.put(new int[]{
                                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                                vertexCount + 2, vertexCount + 3, vertexCount + 0
                        });
                        vertexCount += 4;
                    }

                    // back
                    if (shouldRenderBack(world, ix, iy, iz)) {
                        int tileIndex = tile.back();
                        verticesBuffer.put(new float[]{
                                // @formatter:off
                            xOffset +  0.5f, yOffset +  0.5f, zOffset +  0.5f,   0f, 0f, tileIndex,  0f,  0f,  1f,
                            xOffset + -0.5f, yOffset +  0.5f, zOffset +  0.5f,   0f, 1f, tileIndex,  0f,  0f,  1f,
                            xOffset + -0.5f, yOffset + -0.5f, zOffset +  0.5f,   1f, 1f, tileIndex,  0f,  0f,  1f,
                            xOffset +  0.5f, yOffset + -0.5f, zOffset +  0.5f,   1f, 0f, tileIndex,  0f,  0f,  1f
                                // @formatter:on
                        });

                        indicesBuffer.put(new int[]{
                                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                                vertexCount + 2, vertexCount + 3, vertexCount + 0
                        });
                        vertexCount += 4;
                    }
                }
            }

            if (verticesBuffer.remaining() < 9 * 4 * 6 * world.width() * world.depth()) {
                FloatBuffer newVerticesBuffer = createFloatBuffer(verticesBuffer.capacity() + 9 * 4 * 6 * world.width() * world.depth());
                verticesBuffer.flip();
                newVerticesBuffer.put(verticesBuffer);
                verticesBuffer = newVerticesBuffer;

                IntBuffer newIndiciesBuffer = createIntBuffer(indicesBuffer.capacity() + 6 * 6 * world.width() * world.depth());
                indicesBuffer.flip();
                newIndiciesBuffer.put(indicesBuffer);
                indicesBuffer = newIndiciesBuffer;
            }
        }

        verticesBuffer.flip();
        float[] vertices = new float[verticesBuffer.remaining()];
        verticesBuffer.get(vertices);

        indicesBuffer.flip();
        int[] indices = new int[indicesBuffer.remaining()];
        indicesBuffer.get(indices);

        return new Mesh(vertices, indices, POSITION, TEXCOORD, NORMAL);
    }

    private static boolean shouldRenderBack(World world, int ix, int iy, int iz) {
        if (iz + 1 == world.depth()) return true;
        return world.getBlock(world.converter().index(ix, iy, iz + 1)) == null;
    }

    private static boolean shouldRenderBottom(World world, int ix, int iy, int iz) {
        if (iy == 0) return true;
        return world.getBlock(world.converter().index(ix, iy - 1, iz)) == null;
    }

    private static boolean shouldRenderRight(World world, int ix, int iy, int iz) {
        if (ix + 1 == world.width()) return true;
        return world.getBlock(world.converter().index(ix + 1, iy, iz)) == null;
    }

    private static boolean shouldRenderLeft(World world, int ix, int iy, int iz) {
        if (ix == 0) return true;
        return world.getBlock(world.converter().index(ix - 1, iy, iz)) == null;
    }

    private static boolean shouldRenderTop(World world, int ix, int iy, int iz) {
        if (iy + 1 == world.height()) return true;
        return world.getBlock(world.converter().index(ix, iy + 1, iz)) == null;
    }


    private static boolean shouldRenderFront(World world, int ix, int iy, int iz) {
        if (iz == 0) return true;
        return world.getBlock(world.converter().index(ix, iy, iz - 1)) == null;
    }

}
