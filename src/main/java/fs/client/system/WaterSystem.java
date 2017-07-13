package fs.client.system;

import fs.client.event.BlockGeneratedEvent;
import fs.client.event.BlockUpdatedEvent;
import fs.client.event.TickEvent;
import fs.client.world.Location;
import fs.client.world.Direction;
import fs.client.world.World;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static fs.client.world.World.MAX_WATER_LEVEL;

@Singleton
public class WaterSystem {

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
    if (tick % FLOW_INTERVAL == 0) {
      if (flow()) {
        events.fire(new BlockUpdatedEvent());
      }
    }
  }

  private boolean flow() {
    PriorityQueue<Location> flowCandidates = new PriorityQueue<>(Comparator.comparingInt(this::waterPotential));

    // Get all potential flow candidates
    for (int ix = 0; ix < world.width(); ix ++) {
      for (int iy = 0; iy < world.height(); iy ++) {
        for (int iz = 0; iz < world.depth(); iz ++) {
          Location location = new Location(world, ix, iy, iz);
          int waterLevel = location.block().waterLevel();
          maxFlowOut[location.index()] = waterLevel;

          if (location.block().type() == null) {
            if (isWater(location) || isNeighbouringWater(location)) {
              flowCandidates.add(location);
            }
          }
        }
      }
    }

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
          maxFlowOut[path.get(i).index()]--;
        }

        Location sourceLocation = path.get(0);
        if (sourceLocation != null) {
          int sourceWaterLevel = sourceLocation.block().waterLevel();
          sourceLocation.block().waterLevel(sourceWaterLevel - 1);
          targetLocation.block().waterLevel(targetWaterLevel + 1);
          waterChanged = true;

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
      for (Location neighbour : world.converter().neighbours(cellLocation)) {
        if (maxFlowOut[neighbour.index()] > 0) {
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
      maxFlowOut[cellLocation.index()] = 0;
    }

    return Collections.emptyList();
  }

  private boolean isWater(Location location) {
    return location.block().waterLevel() > 0;
  }

  private boolean isNeighbouringWater(Location location) {
    for (Direction direction: Direction.values()) {
      Location neighbour = direction.neighbour(location);
      if (! neighbour.isWithinWorldLimits()) {
        continue;
      }

      if (isWater(neighbour)) {
        return true;
      }
    }
    return false;
  }

  private int waterPotential(Location location) {
    int waterLevel = location.block().waterLevel();
    return location.y() * (MAX_WATER_LEVEL + 1) + waterLevel;
  }
}
