package fs.math;

public class Interpolators {

    public static float map(float n, float low, float high, float newLow, float newHigh) {
        if (n < low)
            return newLow;
        if (n > high)
            return newHigh;

        float range = (high - low);
        float newRange = (newHigh - newLow);
        return ((n - low) / range) * newRange + newLow;
    }

    public static float lerp(float a, float b, float alpha) {
        if (alpha <= 0)
            return a;
        if (alpha >= 1)
            return b;
        return a + alpha * (b - a);
    }
}
