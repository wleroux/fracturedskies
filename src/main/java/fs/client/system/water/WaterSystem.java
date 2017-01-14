package fs.client.system.water;

import fs.client.async.Dispatcher;
import fs.client.async.GameSystem;
import fs.client.event.UpdateRequested;
import fs.client.event.WaterLevelsUpdated;
import fs.client.event.WorldGenerated;
import fs.client.world.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static fs.client.world.World.MAX_WATER_LEVEL;

public class WaterSystem implements GameSystem {

    private final Dispatcher dispatcher;

    private World world;
    private int[] maxFlowOut;
    private int tick = 0;
    private static final int INTERVAL = 1;

    public WaterSystem(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean canHandle(Object event) {
        return event instanceof WorldGenerated ||
                event instanceof UpdateRequested;
    }

    @Override
    public void accept(Object event, CompletableFuture<Void> future) {
        if (event instanceof WorldGenerated) {
            world = new World(((WorldGenerated) event).world());
            maxFlowOut = new int[world.size()];

        } else if (event instanceof UpdateRequested) {
            tick ++;
            if (tick % INTERVAL == 0) {
                world.waterLevel(world.converter().index(
                        world.width() / 2,
                        world.height() - 1,
                        world.depth() / 2
                ), MAX_WATER_LEVEL);

                if (flow()) {
                    dispatcher.dispatch(new WaterLevelsUpdated(world.waterLevel())).join();
                }
            }
        }

        future.complete(null);
    }

    private boolean flow() {
        PriorityQueue<Integer> flowCandidates = new PriorityQueue<>((a, b) ->
            waterPotential(a) - waterPotential(b)
        );

        // Get all potential flow candidates
        for (int targetIndex = 0; targetIndex < world.size(); targetIndex ++) {
            int waterLevel = world.waterLevel(targetIndex);
            maxFlowOut[targetIndex] = waterLevel;

            if (world.getBlock(targetIndex) == null) {
                if (isWater(targetIndex) || isNeighbouringWater(targetIndex)) {
                    flowCandidates.add(targetIndex);
                }
            }
        }

        // Process water
        boolean waterChanged = false;
        while ( ! flowCandidates.isEmpty() ) {
            // Find path from water source to water target
            int targetIndex = flowCandidates.poll();
            int targetWaterLevel = world.waterLevel(targetIndex);
            if (targetWaterLevel >= MAX_WATER_LEVEL) {
                continue;
            }

            List<Integer> path = find(targetIndex);
            if (! path.isEmpty()) {
                for (int i = 0; i < path.size() - 1; i++) {
                    maxFlowOut[path.get(i)]--;
                }

                int sourceIndex = path.get(0);
                if (sourceIndex != -1) {
                    int sourceWaterLevel = world.waterLevel(sourceIndex);
                    world.waterLevel(sourceIndex, sourceWaterLevel - 1);
                    world.waterLevel(targetIndex, targetWaterLevel + 1);
                    waterChanged = true;

                    flowCandidates.remove(sourceIndex);
                    flowCandidates.add(sourceIndex);
                    if (targetWaterLevel != MAX_WATER_LEVEL) {
                        flowCandidates.add(targetIndex);
                    }
                }
            }
        }

        return waterChanged;
    }

    private List<Integer> find(int startIndex) {
        int targetWaterPotential = waterPotential(startIndex) + 2;

        Map<Integer, Integer> cameFrom = new HashMap<>();
        cameFrom.put(startIndex, null);

        Queue<Integer> unvisitedCells = new LinkedList<>();
        unvisitedCells.add(startIndex);
        while (!unvisitedCells.isEmpty()) {
            int cellIndex = unvisitedCells.poll();

            // Find cell with higher potential
            int cellPotential = waterPotential(cellIndex);
            if (cellPotential >= targetWaterPotential) {

                List<Integer> path = new ArrayList<>();
                path.add(cellIndex);
                while (cameFrom.get(cellIndex) != null) {
                    cellIndex = cameFrom.get(cellIndex);
                    path.add(cellIndex);
                }

                return path;
            }

            // If not higher potential, find neighbours
            for (int neighbour: world.converter().neighbours(cellIndex)) {
                if (maxFlowOut[neighbour] > 0) {
                    if (!cameFrom.containsKey(neighbour)) {
                        unvisitedCells.add(neighbour);
                        cameFrom.put(neighbour, cellIndex);
                    }
                }
            }
        }

        // If we cannot find any path, then any adjacent neighbours would not find any better solution either; discard
        // them so we don't waste time!
        for (Integer cellIndex: cameFrom.keySet()) {
            maxFlowOut[cellIndex] = 0;
        }

        return Collections.emptyList();
    }

    private boolean isWater(int index) {
        return world.waterLevel(index) > 0;
    }

    private boolean isNeighbouringWater(int index) {
        for (int neighbour: world.converter().neighbours(index)) {
            if (isWater(neighbour)) {
                return true;
            }
        }
        return false;
    }

    private int waterPotential(int index) {
        int y = world.converter().y(index);
        int waterLevel = world.waterLevel(index);
        return y * (MAX_WATER_LEVEL + 1) + waterLevel;
    }
}
