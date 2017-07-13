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
    for (int iy = 0; iy < world.height(); iy++) {
      for (int ix = 0; ix < world.width(); ix++) {
        for (int iz = 0; iz < world.depth(); iz++) {
          Location location = new Location(world, ix, iy, iz);
          BlockType type = location.block().type();
          if (type == BlockType.AIR) {
            continue;
          }

          float xOffset = (float) location.x();
          float yOffset = (float) location.y();
          float zOffset = (float) location.z();

          // north
          if (isEmpty(Direction.NORTH.neighbour(location))) {
            int tileIndex = type.front();
            verticesBuffer.put(new float[]{
                // @formatter:off
                xOffset + 0f, yOffset + 1f, zOffset + 0f, 0f, 0f, tileIndex, 0f, -1f, 0f,
                xOffset + 1f, yOffset + 1f, zOffset + 0f, 0f, 1f, tileIndex, 0f, -1f, 0f,
                xOffset + 1f, yOffset + 0f, zOffset + 0f, 1f, 1f, tileIndex, 0f, -1f, 0f,
                xOffset + 0f, yOffset + 0f, zOffset + 0f, 1f, 0f, tileIndex, 0f, -1f, 0f,
                // @formatter:on
            });

            indicesBuffer.put(new int[]{
                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                vertexCount + 2, vertexCount + 3, vertexCount + 0
            });
            vertexCount += 4;
          }


          // up
          if (isEmpty(Direction.UP.neighbour(location))) {
            int tileIndex = type.top();
            verticesBuffer.put(new float[]{
                // @formatter:off
                xOffset + 0f, yOffset + 1f, zOffset + 1f, 0f, 0f, tileIndex, 0f, 1f, 0f,
                xOffset + 1f, yOffset + 1f, zOffset + 1f, 0f, 1f, tileIndex, 0f, 1f, 0f,
                xOffset + 1f, yOffset + 1f, zOffset + 0f, 1f, 1f, tileIndex, 0f, 1f, 0f,
                xOffset + 0f, yOffset + 1f, zOffset + 0f, 1f, 0f, tileIndex, 0f, 1f, 0f,
                // @formatter:on
            });

            indicesBuffer.put(new int[]{
                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                vertexCount + 2, vertexCount + 3, vertexCount + 0
            });
            vertexCount += 4;
          }

          // west
          if (isEmpty(Direction.WEST.neighbour(location))) {
            int tileIndex = type.left();
            verticesBuffer.put(new float[]{
                // @formatter:off
                xOffset + 0f, yOffset + 1f, zOffset + 1f, 0f, 0f, tileIndex, -1f, 0f, 0f,
                xOffset + 0f, yOffset + 1f, zOffset + 0f, 0f, 1f, tileIndex, -1f, 0f, 0f,
                xOffset + 0f, yOffset + 0f, zOffset + 0f, 1f, 1f, tileIndex, -1f, 0f, 0f,
                xOffset + 0f, yOffset + 0f, zOffset + 1f, 1f, 0f, tileIndex, -1f, 0f, 0f,
                // @formatter:on
            });

            indicesBuffer.put(new int[]{
                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                vertexCount + 2, vertexCount + 3, vertexCount + 0
            });
            vertexCount += 4;
          }

          // east
          if (isEmpty(Direction.EAST.neighbour(location))) {
            int tileIndex = type.right();
            verticesBuffer.put(new float[]{
                // @formatter:off
                xOffset + 1f, yOffset + 1f, zOffset + 0f, 0f, 0f, tileIndex, 1f, 0f, 0f,
                xOffset + 1f, yOffset + 1f, zOffset + 1f, 0f, 1f, tileIndex, 1f, 0f, 0f,
                xOffset + 1f, yOffset + 0f, zOffset + 1f, 1f, 1f, tileIndex, 1f, 0f, 0f,
                xOffset + 1f, yOffset + 0f, zOffset + 0f, 1f, 0f, tileIndex, 1f, 0f, 0f,
                // @formatter:on
            });

            indicesBuffer.put(new int[]{
                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                vertexCount + 2, vertexCount + 3, vertexCount + 0
            });
            vertexCount += 4;
          }

          // down
          if (isEmpty(Direction.DOWN.neighbour(location))) {
            int tileIndex = type.bottom();
            verticesBuffer.put(new float[]{
                // @formatter:off
                xOffset + 0f, yOffset + 0f, zOffset + 0f, 0f, 0f, tileIndex, 0f, -1f, 0f,
                xOffset + 1f, yOffset + 0f, zOffset + 0f, 0f, 1f, tileIndex, 0f, -1f, 0f,
                xOffset + 1f, yOffset + 0f, zOffset + 1f, 1f, 1f, tileIndex, 0f, -1f, 0f,
                xOffset + 0f, yOffset + 0f, zOffset + 1f, 1f, 0f, tileIndex, 0f, -1f, 0f,
                // @formatter:on
            });

            indicesBuffer.put(new int[]{
                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                vertexCount + 2, vertexCount + 3, vertexCount + 0
            });
            vertexCount += 4;
          }

          // south
          if (isEmpty(Direction.SOUTH.neighbour(location))) {
            int tileIndex = type.back();
            verticesBuffer.put(new float[]{
                // @formatter:off
                xOffset + 1f, yOffset + 1f, zOffset + 1f, 0f, 0f, tileIndex, 0f, 0f, 1f,
                xOffset + 0f, yOffset + 1f, zOffset + 1f, 0f, 1f, tileIndex, 0f, 0f, 1f,
                xOffset + 0f, yOffset + 0f, zOffset + 1f, 1f, 1f, tileIndex, 0f, 0f, 1f,
                xOffset + 1f, yOffset + 0f, zOffset + 1f, 1f, 0f, tileIndex, 0f, 0f, 1f
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

  private static boolean isEmpty(Location location) {
    if (!location.isWithinWorldLimits()) return true;
    return location.block().type() == BlockType.AIR;
  }
}
