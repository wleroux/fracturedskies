package fs.math;

import java.util.Random;

import static fs.math.Hash.hash;
import static fs.math.Interpolators.lerp;
import static java.lang.Float.floatToRawIntBits;

public class Noise {

    public static float perlin(int seed, float x, float y, float z, int octaves, float persistence) {
        float total = 0;
        float frequency = 1;
        float amplitude = 1;
        float maxValue = 0;
        for(int i = 0; i < octaves; i++) {
            total += perlin(seed, x * frequency, y * frequency, z * frequency) * amplitude;
            frequency *= 2;

            maxValue += amplitude;
            amplitude *= persistence;
        }

        return total / maxValue;
    }


    public static float perlin(int seed, float x, float y, float z) {
        int sx = (int) Math.floor((double) x);
        int sy = (int) Math.floor((double) y);
        int sz = (int) Math.floor((double) z);
        int ex = sx + 1;
        int ey = sy + 1;
        int ez = sz + 1;

        float x0y0z0 = noise(seed, sx, sy, sz);
        float x1y0z0 = noise(seed, ex, sy, sz);
        float xy0z0 = lerp(x0y0z0, x1y0z0, x - sx);

        float x0y1z0 = noise(seed, sx, ey, sz);
        float x1y1z0 = noise(seed, ex, ey, sz);
        float xy1z0 = lerp(x0y1z0, x1y1z0, x - sx);

        float xyz0 = lerp(xy0z0, xy1z0, y - sy);

        float x0y0z1 = noise(seed, sx, sy, ez);
        float x1y0z1 = noise(seed, ex, sy, ez);
        float xy0z1 = lerp(x0y0z1, x1y0z1, x - sx);

        float x0y1z1 = noise(seed, sx, ey, ez);
        float x1y1z1 = noise(seed, ex, ey, ez);
        float xy1z1 = lerp(x0y1z1, x1y1z1, x - sx);

        float xyz1 = lerp(xy0z1, xy1z1, y - sy);

        return lerp(xyz0, xyz1, z - sz);
    }

    public static float noise(int seed, float x, float y, float z) {
        return new Random(hash(
                seed,
                floatToRawIntBits(x),
                floatToRawIntBits(y),
                floatToRawIntBits(z)
        )).nextFloat();
    }
}
