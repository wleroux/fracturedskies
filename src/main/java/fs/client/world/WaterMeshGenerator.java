package fs.client.world;

import fs.client.ui.primitive.mesh.Mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static fs.client.Game.CHUNK_SIZE;
import static fs.client.ui.primitive.OpenGLComponent.*;
import static fs.client.world.Direction.*;
import static fs.client.world.World.MAX_WATER_LEVEL;
import static fs.math.Interpolators.map;
import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.BufferUtils.createIntBuffer;

public class WaterMeshGenerator {
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
          BlockState block = location.block();
          if (block.type() != BlockType.AIR) {
            continue;
          }

          int curWaterLevel = block.waterLevel();
          if (curWaterLevel == 0) {
            continue;
          }

          float xOffset = (float) ix;
          float yOffset = (float) iy;
          float zOffset = (float) iz;
          float curWaterHeight = waterHeight(curWaterLevel);

          // north
          Location northLocation = location.neighbour(NORTH);
          if (isEmpty(northLocation)) {
            int adjWaterLevel = iz == 0 ? 0 : northLocation.block().waterLevel();
            float adjWaterHeight = waterHeight(adjWaterLevel);

            if (adjWaterHeight < curWaterHeight) {
              verticesBuffer.put(new float[]{
                  // @formatter:off
                  xOffset + 0f, yOffset + curWaterHeight, zOffset + 0f, 0f, 0f, 0f, 0f, -1f, 0f,
                  xOffset + 1f, yOffset + curWaterHeight, zOffset + 0f, 0f, 1f, 0f, 0f, -1f, 0f,
                  xOffset + 1f, yOffset + adjWaterHeight, zOffset + 0f, 1f, 1f, 0f, 0f, -1f, 0f,
                  xOffset + 0f, yOffset + adjWaterHeight, zOffset + 0f, 1f, 0f, 0f, 0f, -1f, 0f,
                  // @formatter:on
              });

              indicesBuffer.put(new int[]{
                  vertexCount + 0, vertexCount + 1, vertexCount + 2,
                  vertexCount + 2, vertexCount + 3, vertexCount + 0
              });
              vertexCount += 4;
            }
          }


          // up
          Location upLocation = location.neighbour(UP);
          if (isEmpty(upLocation) || curWaterLevel != MAX_WATER_LEVEL) {
            int adjWaterLevel = upLocation.isWithinWorldLimits() ? upLocation.block().waterLevel() : 0;
            if (curWaterLevel != MAX_WATER_LEVEL || adjWaterLevel == 0) {
              verticesBuffer.put(new float[]{
                  // @formatter:off
                  xOffset + 0f, yOffset + curWaterHeight, zOffset + 1f, 0f, 0f, 0f, 0f, 1f, 0f,
                  xOffset + 1f, yOffset + curWaterHeight, zOffset + 1f, 0f, 1f, 0f, 0f, 1f, 0f,
                  xOffset + 1f, yOffset + curWaterHeight, zOffset + 0f, 1f, 1f, 0f, 0f, 1f, 0f,
                  xOffset + 0f, yOffset + curWaterHeight, zOffset + 0f, 1f, 0f, 0f, 0f, 1f, 0f,
                  // @formatter:on
              });

              indicesBuffer.put(new int[]{
                  vertexCount + 0, vertexCount + 1, vertexCount + 2,
                  vertexCount + 2, vertexCount + 3, vertexCount + 0
              });
              vertexCount += 4;
            }
          }

          // west
          Location westLocation = location.neighbour(WEST);
          if (isEmpty(westLocation)) {
            int adjWaterLevel = westLocation.isWithinWorldLimits() ? westLocation.block().waterLevel() : 0;
            float adjWaterHeight = waterHeight(adjWaterLevel);

            if (adjWaterHeight < curWaterHeight) {
              verticesBuffer.put(new float[]{
                  // @formatter:off
                  xOffset + 0f, yOffset + curWaterHeight, zOffset + 1f, 0f, 0f, 0f, -1f, 0f, 0f,
                  xOffset + 0f, yOffset + curWaterHeight, zOffset + 0f, 0f, 1f, 0f, -1f, 0f, 0f,
                  xOffset + 0f, yOffset + adjWaterHeight, zOffset + 0f, 1f, 1f, 0f, -1f, 0f, 0f,
                  xOffset + 0f, yOffset + adjWaterHeight, zOffset + 1f, 1f, 0f, 0f, -1f, 0f, 0f,
                  // @formatter:on
              });

              indicesBuffer.put(new int[]{
                  vertexCount + 0, vertexCount + 1, vertexCount + 2,
                  vertexCount + 2, vertexCount + 3, vertexCount + 0
              });
              vertexCount += 4;
            }
          }

          // east
          Location eastLocation = location.neighbour(EAST);
          if (isEmpty(eastLocation)) {
            int adjWaterLevel = eastLocation.isWithinWorldLimits() ? eastLocation.block().waterLevel() : 0;
            float adjWaterHeight = waterHeight(adjWaterLevel);

            if (adjWaterHeight < curWaterHeight) {
              verticesBuffer.put(new float[]{
                  // @formatter:off
                  xOffset + 1f, yOffset + curWaterHeight, zOffset + 0f, 0f, 0f, 0f, 1f, 0f, 0f,
                  xOffset + 1f, yOffset + curWaterHeight, zOffset + 1f, 0f, 1f, 0f, 1f, 0f, 0f,
                  xOffset + 1f, yOffset + adjWaterHeight, zOffset + 1f, 1f, 1f, 0f, 1f, 0f, 0f,
                  xOffset + 1f, yOffset + adjWaterHeight, zOffset + 0f, 1f, 0f, 0f, 1f, 0f, 0f,
                  // @formatter:on
              });

              indicesBuffer.put(new int[]{
                  vertexCount + 0, vertexCount + 1, vertexCount + 2,
                  vertexCount + 2, vertexCount + 3, vertexCount + 0
              });
              vertexCount += 4;
            }
          }

          // down
          Location downLocation = location.neighbour(DOWN);
          if (isEmpty(downLocation)) {
            int adjWaterLevel = downLocation.isWithinWorldLimits() ? downLocation.block().waterLevel() : 0;
            if (adjWaterLevel != MAX_WATER_LEVEL) {
              verticesBuffer.put(new float[]{
                  // @formatter:off
                  xOffset + 0f, yOffset + 0f, zOffset + 0f, 0f, 0f, 0f, 0f, -1f, 0f,
                  xOffset + 1f, yOffset + 0f, zOffset + 0f, 0f, 1f, 0f, 0f, -1f, 0f,
                  xOffset + 1f, yOffset + 0f, zOffset + 1f, 1f, 1f, 0f, 0f, -1f, 0f,
                  xOffset + 0f, yOffset + 0f, zOffset + 1f, 1f, 0f, 0f, 0f, -1f, 0f,
                  // @formatter:on
              });

              indicesBuffer.put(new int[]{
                  vertexCount + 0, vertexCount + 1, vertexCount + 2,
                  vertexCount + 2, vertexCount + 3, vertexCount + 0
              });
              vertexCount += 4;
            }
          }

          // south
          Location southLocation = location.neighbour(SOUTH);
          if (isEmpty(southLocation)) {
            int adjWaterLevel = southLocation.isWithinWorldLimits() ? southLocation.block().waterLevel() : 0;
            float adjWaterHeight = waterHeight(adjWaterLevel);

            if (adjWaterHeight < curWaterHeight) {
              verticesBuffer.put(new float[]{
                  // @formatter:off
                  xOffset + 1f, yOffset + curWaterHeight, zOffset + 1f, 0f, 0f, 0f, 0f, 0f, 1f,
                  xOffset + 0f, yOffset + curWaterHeight, zOffset + 1f, 0f, 1f, 0f, 0f, 0f, 1f,
                  xOffset + 0f, yOffset + adjWaterHeight, zOffset + 1f, 1f, 1f, 0f, 0f, 0f, 1f,
                  xOffset + 1f, yOffset + adjWaterHeight, zOffset + 1f, 1f, 0f, 0f, 0f, 0f, 1f
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

    return new Mesh(vertices, indices, POSITION, TEXCOORD, NORMAL);
  }

  private static float waterHeight(int waterLevel) {
    return map(waterLevel, 0, MAX_WATER_LEVEL, 0f, 1f);
  }

  private static boolean isEmpty(Location location) {
    if (!location.isWithinWorldLimits()) return true;
    return location.block().type() == BlockType.AIR;
  }
}
