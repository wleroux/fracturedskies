package fs.client.world;

import fs.client.ui.game.Location;
import fs.client.ui.primitive.mesh.Mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static fs.client.ui.primitive.OpenGLComponent.*;
import static fs.client.world.World.MAX_WATER_LEVEL;
import static fs.math.Interpolators.map;
import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.BufferUtils.createIntBuffer;

public class WaterMeshGenerator {
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
          BlockState block = location.block();
          if (block.type() != null) {
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

          // front
          Location frontLocation = Direction.NORTH.neighbour(location);
          if (isEmpty(frontLocation)) {
            int adjWaterLevel = iz == 0 ? 0 : frontLocation.block().waterLevel();
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


          // top
          Location topLocation = Direction.UP.neighbour(location);
          if (isEmpty(topLocation)) {
            int adjWaterLevel = topLocation.isWithinWorldLimits() ? topLocation.block().waterLevel() : 0;
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

          // left
          Location leftLocation = Direction.WEST.neighbour(location);
          if (isEmpty(leftLocation)) {
            int adjWaterLevel = leftLocation.isWithinWorldLimits() ? leftLocation.block().waterLevel() : 0;
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

          // right
          Location rightLocation = Direction.EAST.neighbour(location);
          if (isEmpty(rightLocation)) {
            int adjWaterLevel = rightLocation.isWithinWorldLimits() ? rightLocation.block().waterLevel() : 0;
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

          // bottom
          Location bottomLocation = Direction.DOWN.neighbour(location);
          if (isEmpty(bottomLocation)) {
            int adjWaterLevel = bottomLocation.isWithinWorldLimits() ? bottomLocation.block().waterLevel() : 0;
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

          // back
          Location backLocation = Direction.SOUTH.neighbour(location);
          if (isEmpty(backLocation)) {
            int adjWaterLevel = backLocation.isWithinWorldLimits() ? backLocation.block().waterLevel() : 0;
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

  private static float waterHeight(int waterLevel) {
    return map(waterLevel, 0, MAX_WATER_LEVEL, 0f, 1f);
  }

  private static boolean isEmpty(Location location) {
    if (!location.isWithinWorldLimits()) return true;
    return location.block().type() == null;
  }
}
