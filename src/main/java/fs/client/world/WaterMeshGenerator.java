package fs.client.world;

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
          if (world.getBlock(world.converter().index(ix, iy, iz)) != null) {
            continue;
          }

          int curWaterLevel = world.waterLevel(world.converter().index(ix, iy, iz));
          if (curWaterLevel == 0) {
            continue;
          }

          float xOffset = (float) ix;
          float yOffset = (float) iy;
          float zOffset = (float) iz;
          float curWaterHeight = waterHeight(curWaterLevel);

          // front
          if (shouldRenderFront(world, ix, iy, iz)) {
            int adjWaterLevel = iz == 0 ? 0 : world.waterLevel(world.converter().index(ix, iy, iz - 1));
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
          if (shouldRenderTop(world, ix, iy, iz)) {
            int adjWaterLevel = (iy + 1 == world.height()) ? 0 : world.waterLevel(world.converter().index(ix, iy + 1, iz));
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
          if (shouldRenderLeft(world, ix, iy, iz)) {
            int adjWaterLevel = ix == 0 ? 0 : world.waterLevel(world.converter().index(ix - 1, iy, iz));
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
          if (shouldRenderRight(world, ix, iy, iz)) {
            int adjWaterLevel = (ix + 1 == world.width()) ? 0 : world.waterLevel(world.converter().index(ix + 1, iy, iz));
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
          if (shouldRenderBottom(world, ix, iy, iz)) {
            int adjWaterLevel = (iy == 0) ? 0 : world.waterLevel(world.converter().index(ix, iy - 1, iz));
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
          if (shouldRenderBack(world, ix, iy, iz)) {
            int adjWaterLevel = (iz + 1 == world.depth()) ? 0 : world.waterLevel(world.converter().index(ix, iy, iz + 1));
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
    return true;
  }


  private static boolean shouldRenderFront(World world, int ix, int iy, int iz) {
    if (iz == 0) return true;
    return world.getBlock(world.converter().index(ix, iy, iz - 1)) == null;
  }

}
