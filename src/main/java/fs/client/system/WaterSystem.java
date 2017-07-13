package fs.client.system;

import fs.client.event.BlockGeneratedEvent;
import fs.client.event.BlockUpdatedEvent;
import fs.client.event.TickEvent;
import fs.client.world.World;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static fs.client.world.World.MAX_WATER_LEVEL;

@Singleton
public class WaterSystem {

  private static final int RAIN_INTERVAL = 30;
  private static final int FLOW_INTERVAL = 5;
  @Inject
  private Event<Object> events;

  @Inject
  private World world;

  private int[] maxFlowOut;
  private int tick = 0;

  public void onWorldGenerated(@Observes BlockGeneratedEvent event) {
    maxFlowOut = new int[world.size()];
  }

  public void onUpdateRequested(@Observes TickEvent event) {
    tick++;
    if (tick % RAIN_INTERVAL == 0) {
      world.waterLevel(world.converter().index(
          world.width() / 2,
          world.height() - 1,
          world.depth() / 2
      ), 1);
      events.fire(new BlockUpdatedEvent());
    }

    if (tick % FLOW_INTERVAL == 0) {
      if (flow()) {
        events.fire(new BlockUpdatedEvent());
      }
    }
  }

  private boolean flow() {
    PriorityQueue<Integer> flowCandidates = new PriorityQueue<>(Comparator.comparingInt(this::waterPotential));

    // Get all potential flow candidates
    for (int targetIndex = 0; targetIndex < world.size(); targetIndex++) {
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
    while (!flowCandidates.isEmpty()) {
      // Find path from water source to water target
      int targetIndex = flowCandidates.poll();
      int targetWaterLevel = world.waterLevel(targetIndex);
      if (targetWaterLevel >= MAX_WATER_LEVEL) {
        continue;
      }

      List<Integer> path = find(targetIndex);
      if (!path.isEmpty()) {
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

      // If not higher potential, findComponentAt neighbours
      for (int neighbour : world.converter().neighbours(cellIndex)) {
        if (maxFlowOut[neighbour] > 0) {
          if (!cameFrom.containsKey(neighbour)) {
            unvisitedCells.add(neighbour);
            cameFrom.put(neighbour, cellIndex);
          }
        }
      }
    }

    // If we cannot findComponentAt any path, then any adjacent neighbours would not findComponentAt any better solution either; discard
    // them so we don't waste time!
    for (Integer cellIndex : cameFrom.keySet()) {
      maxFlowOut[cellIndex] = 0;
    }

    return Collections.emptyList();
  }

  private boolean isWater(int index) {
    return world.waterLevel(index) > 0;
  }

  private boolean isNeighbouringWater(int index) {
    for (int neighbour : world.converter().neighbours(index)) {
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
