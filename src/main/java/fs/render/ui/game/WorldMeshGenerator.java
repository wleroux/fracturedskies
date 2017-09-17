package fs.render.ui.game;

import fs.render.ui.primitive.mesh.Mesh;
import fs.block.BlockType;
import fs.render.ui.primitive.OpenGLComponent;
import fs.world.Direction;
import fs.world.Location;
import fs.world.World;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static fs.Game.CHUNK_SIZE;
import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.BufferUtils.createIntBuffer;

public class WorldMeshGenerator {
  public static Mesh generateMesh(World world, int chunkX, int chunkY, int chunkZ) {
    FloatBuffer verticesBuffer = createFloatBuffer(
        9 * 4 * 6 * CHUNK_SIZE * CHUNK_SIZE
    );
    IntBuffer indicesBuffer = createIntBuffer(
        6 * 6 * CHUNK_SIZE * CHUNK_SIZE
    );

    int vertexCount = 0;
    for (int iy = chunkY * CHUNK_SIZE; iy < (chunkY + 1) * CHUNK_SIZE; iy++) {
      for (int ix = chunkX * CHUNK_SIZE; ix < (chunkX + 1) * CHUNK_SIZE; ix++) {
        for (int iz = chunkZ * CHUNK_SIZE; iz < (chunkZ + 1) * CHUNK_SIZE; iz++) {
          Location location = world.location(ix, iy, iz);
          BlockType type = location.block().type();
          if (type == BlockType.AIR) {
            continue;
          }

          float xOffset = (float) location.x();
          float yOffset = (float) location.y();
          float zOffset = (float) location.z();

          // north
          if (isEmpty(location.neighbour(Direction.NORTH))) {
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
          if (isEmpty(location.neighbour(Direction.UP))) {
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
          if (isEmpty(location.neighbour(Direction.WEST))) {
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
          if (isEmpty(location.neighbour(Direction.EAST))) {
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
          if (isEmpty(location.neighbour(Direction.DOWN))) {
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
          if (isEmpty(location.neighbour(Direction.SOUTH))) {
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

      if (verticesBuffer.remaining() < 9 * 4 * 6 * CHUNK_SIZE * CHUNK_SIZE) {
        FloatBuffer newVerticesBuffer = createFloatBuffer(verticesBuffer.capacity() + 9 * 4 * 6 * CHUNK_SIZE * CHUNK_SIZE);
        verticesBuffer.flip();
        newVerticesBuffer.put(verticesBuffer);
        verticesBuffer = newVerticesBuffer;

        IntBuffer newIndiciesBuffer = createIntBuffer(indicesBuffer.capacity() + 6 * 6 * CHUNK_SIZE * CHUNK_SIZE);
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

    return new Mesh(vertices, indices, OpenGLComponent.POSITION, OpenGLComponent.TEXCOORD, OpenGLComponent.NORMAL);
  }

  private static boolean isEmpty(Location location) {
    if (!location.isWithinWorldLimits()) return true;
    return location.block().type() == BlockType.AIR;
  }
}
