package fs.client.system;

import fs.client.event.WorldGeneratedEvent;
import fs.client.event.TickEvent;
import fs.client.event.WaterUpdatedEvent;
import fs.client.world.BlockType;
import fs.client.world.Location;
import fs.client.world.World;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static fs.client.world.World.MAX_WATER_LEVEL;
import static java.util.Comparator.comparingInt;

@Singleton
public class WaterSystem {

  private static final int FLOW_INTERVAL = 1;

  @Inject
  private Event<Object> events;

  @Inject
  private World world;

  private int[] maxFlowOut;
  private boolean[] considered;

  private int tick = 0;
  private static Comparator<Location> WATER_POTENTIAL_COMPARATOR = comparingInt(WaterSystem::waterPotential);

  public void onWorldGenerated(@Observes WorldGeneratedEvent event) {
    maxFlowOut = new int[world.size()];
    considered = new boolean[world.size()];
  }

  public void onUpdateRequested(@Observes TickEvent event) {
    tick++;
    if (tick % FLOW_INTERVAL == 0) {
      flow();
    }
  }


  private boolean flow() {
    // Reset flow
    Arrays.fill(maxFlowOut, 0);
    Arrays.fill(considered, false);

    // Get all potential flow candidates
    List<Location> unsortedCandidates = new ArrayList<>();
    for (Location location: world.locations()) {
      if (isWater(location)) {
        if (!considered[location.index()]) {
          unsortedCandidates.add(location);
          considered[location.index()] = true;
        }

        for (Location neighbour: location.neighbours().values()) {
          if (neighbour.block().type() == BlockType.AIR) {
            if (!considered[neighbour.index()]) {
              unsortedCandidates.add(neighbour);
            }
          }
        }
      }
    }

    PriorityQueue<Location> flowCandidates = new PriorityQueue<>(WATER_POTENTIAL_COMPARATOR);
    Collections.shuffle(unsortedCandidates);
    flowCandidates.addAll(unsortedCandidates);

    // Process water
    boolean waterChanged = false;
    while (!flowCandidates.isEmpty()) {
      // Find path from water source to water target
      Location targetLocation = flowCandidates.poll();
      int targetWaterLevel = targetLocation.block().waterLevel();
      if (targetWaterLevel >= MAX_WATER_LEVEL) {
        continue;
      }

      List<Location> path = find(targetLocation);
      if (!path.isEmpty()) {
        for (int i = 0; i < path.size() - 1; i++) {
          maxFlowOut[path.get(i).index()]++;
        }

        Location sourceLocation = path.get(0);
        if (sourceLocation != null) {
          int sourceWaterLevel = sourceLocation.block().waterLevel();
          sourceLocation.block().waterLevel(sourceWaterLevel - 1);
          targetLocation.block().waterLevel(targetWaterLevel + 1);
          waterChanged = true;
          events.fire(new WaterUpdatedEvent(sourceLocation));
          events.fire(new WaterUpdatedEvent(targetLocation));


          flowCandidates.remove(sourceLocation);
          flowCandidates.add(sourceLocation);
          if (targetWaterLevel != MAX_WATER_LEVEL) {
            flowCandidates.add(targetLocation);
          }
        }
      }
    }

    return waterChanged;
  }

  private List<Location> find(Location startLocation) {
    int targetWaterPotential = waterPotential(startLocation) + 2;

    Map<Location, Location> cameFrom = new HashMap<>();
    cameFrom.put(startLocation, null);

    Queue<Location> unvisitedCells = new LinkedList<>();
    unvisitedCells.add(startLocation);
    while (!unvisitedCells.isEmpty()) {
      Location cellLocation = unvisitedCells.poll();

      // Find cell with higher potential
      int cellPotential = waterPotential(cellLocation);
      if (cellPotential >= targetWaterPotential) {
        List<Location> path = new ArrayList<>();
        path.add(cellLocation);
        while (cameFrom.get(cellLocation) != null) {
          cellLocation = cameFrom.get(cellLocation);
          path.add(cellLocation);
        }

        return path;
      }

      // If not higher potential, findComponentAt neighbours
      for (Location neighbour : cellLocation.neighbours().values()) {
        if (maxFlowOut[neighbour.index()] < neighbour.block().waterLevel()) {
          if (!cameFrom.containsKey(neighbour)) {
            unvisitedCells.add(neighbour);
            cameFrom.put(neighbour, cellLocation);
          }
        }
      }
    }

    // If we cannot findComponentAt any path, then any adjacent neighbours would not findComponentAt any better solution either; discard
    // them so we don't waste time!
    for (Location cellLocation : cameFrom.keySet()) {
      maxFlowOut[cellLocation.index()] = cellLocation.block().waterLevel();
    }

    return Collections.emptyList();
  }

  private static int waterPotential(Location location) {
    int waterLevel = location.block().waterLevel();
    return location.y() * (MAX_WATER_LEVEL + 1) + waterLevel;
  }

  public boolean isWater(Location location) {
    return location.block().waterLevel() > 0;
  }
}
