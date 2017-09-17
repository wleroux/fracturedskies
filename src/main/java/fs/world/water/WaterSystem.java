package fs.world.water;

import fs.event.LocationUpdatedEvent;
import fs.event.WorldGeneratedEvent;
import fs.event.game.TickEvent;
import fs.event.WaterUpdatedEvent;
import fs.block.BlockType;
import fs.world.Location;
import fs.world.World;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static fs.world.World.MAX_WATER_LEVEL;
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
  private boolean[] disturbed;

  private int tick = 0;
  private static Comparator<Location> WATER_POTENTIAL_COMPARATOR = comparingInt(WaterSystem::waterPotential);

  public void onWorldGenerated(@Observes WorldGeneratedEvent event) {
    maxFlowOut = new int[world.size()];
    considered = new boolean[world.size()];
    disturbed = new boolean[world.size()];
  }

  public void onUpdateRequested(@Observes TickEvent event) {
    tick++;
    if (tick % FLOW_INTERVAL == 0) {
      flow();
    }
  }

  public void onDisruption(@Observes LocationUpdatedEvent event) {
    disturbed[event.location().index()] = true;
  }


  private boolean flow() {
    // Get all potential flow candidates
    PriorityQueue<Location> flowCandidates = flowCandidates();

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

  private PriorityQueue<Location> flowCandidates() {
    Arrays.fill(maxFlowOut, 0);
    int w = world.width(), h = world.height(), d = world.depth();
    List<Location> unsortedCandidates = new ArrayList<>();
    int index = 0;
    for (int iz = 0; iz < d; iz ++) {
      for (int iy = 0; iy < h; iy++) {
        for (int ix = 0; ix < w; ix ++) {
          if (disturbed[index]) {
            disturbed[index] = false;
            Location location = world.location(ix, iy, iz);
            for (Location adjacentLocations: findAdjacentLocations(location)) {
              maxFlowOut[adjacentLocations.index()] = adjacentLocations.block().waterLevel();
              disturbed[adjacentLocations.index()] = false;
              unsortedCandidates.add(adjacentLocations);
            }
          }
          index ++;
        }
      }
    }
    Collections.shuffle(unsortedCandidates);

    PriorityQueue<Location> flowCandidates = new PriorityQueue<>(WATER_POTENTIAL_COMPARATOR);
    flowCandidates.addAll(unsortedCandidates);
    return flowCandidates;
  }

  /**
   * @param startLocation
   * @return
   */
  private List<Location> findAdjacentLocations(Location startLocation) {
    Arrays.fill(considered, false);
    considered[startLocation.index()] = true;
    Queue<Location> unvisitedCells = new LinkedList<>();
    unvisitedCells.add(startLocation);
    List<Location> locations = new ArrayList<>();
    while (!unvisitedCells.isEmpty()) {
      Location location = unvisitedCells.poll();
      if (location.block().type() == BlockType.AIR)
        locations.add(location);

      for (Location neighbour : location.neighbours().values()) {
        if (considered[neighbour.index()])
          continue;
        if (isWater(location) || isWater(neighbour)) {
          considered[neighbour.index()] = true;
          unvisitedCells.add(neighbour);
        }
      }
    }

    return locations;
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

      // If not higher potential, check neighbours for higher potential
      for (Location neighbour : cellLocation.neighbours().values()) {
        if (maxFlowOut[neighbour.index()] > 0) {
          if (!cameFrom.containsKey(neighbour)) {
            unvisitedCells.add(neighbour);
            cameFrom.put(neighbour, cellLocation);
          }
        }
      }
    }

    // If no neighbours have higher water potential, then any higher water potential neighbours will not find any better solution either; don't waste time processing them
    for (Location cellLocation : cameFrom.keySet()) {
      maxFlowOut[cellLocation.index()] = 0;
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
