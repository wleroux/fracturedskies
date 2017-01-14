package fs.client.event;

/**
 * Created by FracturedSkies on 1/14/2017.
 */
public class WaterLevelsUpdated {
    private final int[] waterLevel;

    public WaterLevelsUpdated(int[] waterLevel) {
        this.waterLevel = waterLevel;
    }

    public int[] waterLevel() {
        return waterLevel;
    }
}
