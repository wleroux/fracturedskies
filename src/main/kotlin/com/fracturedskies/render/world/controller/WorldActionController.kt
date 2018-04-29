package com.fracturedskies.render.world.controller

import com.fracturedskies.World
import com.fracturedskies.api.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.Vector3i.Companion.area
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.task.api.TaskPriority
import com.fracturedskies.water.api.MAX_WATER_LEVEL
import org.lwjgl.glfw.GLFW.*
import java.lang.Integer.*

// Actions
data class WorldMouseClick(val world: World, val worldPos: Vector3, val worldDir: Vector3, val action: Int, val button: Int, val mods: Int)
data class WorldMouseMove(val world: World, val worldPos: Vector3, val worldDir: Vector3)
interface WorldActionController {
  fun onClick(worldMouseClick: WorldMouseClick, sliceHeight: Int) = Unit
  fun onMove(worldMouseMove: WorldMouseMove, sliceHeight: Int) = Unit
  fun onUpdate(world: World, dt: Float) = Unit
  fun area(world: World): Pair<Vector3i, Vector3i>? = null
  fun areaColor(world: World): Color4 = Color4(255, 255, 255, 48)
}

object NoopActionController: WorldActionController

// Spawn colonist
object SpawnColonistActionController : WorldActionController {
  override fun onClick(worldMouseClick: WorldMouseClick, sliceHeight: Int) {
    val (world, worldPos, worldDir, action, _, _) = worldMouseClick
    if (action == GLFW_RELEASE) {
      val raycastHit = raycast(world.blocks, worldPos, worldDir)
          .filter { it.position.y < sliceHeight }
          .filter { it.obj.type != BlockAir }
          .firstOrNull()
      if (raycastHit != null) {
        val pos = raycastHit.position + raycastHit.faces.first()
        if (world.has(pos) && world.blocks[pos].type == BlockAir)
          send(ColonistSpawned(Id(), pos, Cause.of(this)))
      }
    }
  }
}

// Add Block
class AddBlockActionController(private val blockType: BlockType): WorldActionController {
  private var firstBlock: Vector3i? = null
  override fun onClick(worldMouseClick: WorldMouseClick, sliceHeight: Int) {
    val (world, _, _, action, _, _) = worldMouseClick
    if (action == GLFW_PRESS) {
      firstBlock = position
    } else if (action == GLFW_RELEASE) {
      if (firstBlock != null && position != null) {
        val secondBlock = position!!
        val xRange = min(firstBlock!!.x, secondBlock.x)..max(firstBlock!!.x, secondBlock.x)
        val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
        val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)
        area(xRange, yRange, zRange)
            .filter { world.blocks.has(it) && world.blocks[it].type == BlockAir }
            .forEach {
              send(TaskCreated(Id(), TaskPlaceBlock(it, blockType), TaskPriority.AVERAGE, Cause.of(this)))
            }
      }
      firstBlock = null
    }
  }

  private var position: Vector3i? = null
  override fun onMove(worldMouseMove: WorldMouseMove, sliceHeight: Int) {
    val (world, worldPos, worldDir) = worldMouseMove
    val raycastHit = raycast(world.blocks, worldPos, worldDir)
        .filter { it.position.y < sliceHeight }
        .filter { it.obj.type != BlockAir }
        .firstOrNull()
    position = raycastHit?.let { raycastHit.position + raycastHit.faces.first() }
  }

  override fun area(world: World): Pair<Vector3i, Vector3i>? {
    return if (firstBlock != null && position != null) {
      val secondBlock = position!!
      val xRange = min(firstBlock!!.x, secondBlock.x)..max(firstBlock!!.x, secondBlock.x)
      val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
      val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)
      Vector3i(xRange.start, yRange.start, zRange.start) to Vector3i(xRange.endInclusive + 1, yRange.endInclusive + 1, zRange.endInclusive + 1)
    } else {
      super.area(world)
    }
  }

  override fun areaColor(world: World): Color4 = Color4(128, 255, 128, 64)
}

// Remove Block
object RemoveBlockBlockActionController: WorldActionController {
  private var firstBlock: Vector3i? = null
  override fun onClick(worldMouseClick: WorldMouseClick, sliceHeight: Int) {
    val (world, _, _, action, _, _) = worldMouseClick
    if (action == GLFW_PRESS) {
      firstBlock = position
    } else if (action == GLFW_RELEASE) {
      if (firstBlock != null && position != null) {
        val secondBlock = position!!
        val xRange = min(firstBlock!!.x, secondBlock.x)..max(firstBlock!!.x, secondBlock.x)
        val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
        val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)
        area(xRange, yRange, zRange)
            .filter { world.blocks.has(it) && world.blocks[it].type != BlockAir }
            .forEach {
              send(TaskCreated(Id(), TaskRemoveBlock(it), TaskPriority.AVERAGE, Cause.of(this)))
            }
      }
      firstBlock = null
    }
  }

  private var position: Vector3i? = null
  override fun onMove(worldMouseMove: WorldMouseMove, sliceHeight: Int) {
    val (world, worldPos, worldDir) = worldMouseMove
    val raycastHit = raycast(world.blocks, worldPos, worldDir)
        .filter { it.position.y < sliceHeight }
        .filter { it.obj.type != BlockAir }
        .firstOrNull()
    position = raycastHit?.let { raycastHit.position }
  }

  override fun area(world: World): Pair<Vector3i, Vector3i>? {
    return if (firstBlock != null && position != null) {
      val secondBlock = position!!
      val xRange = min(firstBlock!!.x, secondBlock.x)..max(firstBlock!!.x, secondBlock.x)
      val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
      val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)
      Vector3i(xRange.start, yRange.start, zRange.start) to Vector3i(xRange.endInclusive + 1, yRange.endInclusive + 1, zRange.endInclusive + 1)
    } else {
      super.area(world)
    }
  }

  override fun areaColor(world: World): Color4 = Color4(255, 128, 128, 64)
}

// Add Water
object AddWaterBlockActionController: WorldActionController {
  private var isOn = false
  private var position: Vector3i? = null
  override fun onClick(worldMouseClick: WorldMouseClick, sliceHeight: Int) {
    val (world, worldPos, worldDir, action, _, _) = worldMouseClick
    isOn = (action == GLFW_PRESS)
    onMove(WorldMouseMove(world, worldPos, worldDir), sliceHeight)
  }

  override fun onMove(worldMouseMove: WorldMouseMove, sliceHeight: Int) {
    val (world, worldPos, worldDir) = worldMouseMove
    val raycastHit = raycast(world.blocks, worldPos, worldDir)
        .filter { it.position.y < sliceHeight }
        .filter { it.obj.type != BlockAir }
        .firstOrNull()
    position = raycastHit?.let { raycastHit.position + raycastHit.faces.first() }
  }

  override fun onUpdate(world: World, dt: Float) {
    if (isOn) {
      position?.let { position ->
        if (!world.blocks.has(position) || world.blocks[position].type != BlockAir) return
        if (world.blocks[position].waterLevel >= MAX_WATER_LEVEL) return
        send(BlockWaterLevelUpdated(mutableMapOf(position to MAX_WATER_LEVEL), Cause.of(this)))
      }
    }
  }

  override fun area(world: World): Pair<Vector3i, Vector3i>? {
    return if (position != null) {
      position!! to (position!! + Vector3i(1,1,1))
    } else {
      super.area(world)
    }
  }

  override fun areaColor(world: World): Color4 = Color4(128, 128, 255, 64)
}

// Add Zone
object AddZoneActionController: WorldActionController {
  private var firstBlock: Vector3i? = null
  override fun onClick(worldMouseClick: WorldMouseClick, sliceHeight: Int) {
    val (_, _, _, action, _, _) = worldMouseClick
    if (action == GLFW_PRESS) {
      firstBlock = position
    } else if (action == GLFW_RELEASE) {
      if (firstBlock != null && position != null) {
        val secondBlock = position!!
        val xRange = min(firstBlock!!.x, secondBlock.x)..max(firstBlock!!.x, secondBlock.x)
        val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
        val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)
        send(ZoneCreated(Id(), area(xRange, yRange, zRange), Cause.of(this)))
      }
      firstBlock = null
    }
  }

  private var position: Vector3i? = null
  override fun onMove(worldMouseMove: WorldMouseMove, sliceHeight: Int) {
    val (world, worldPos, worldDir) = worldMouseMove
    val raycastHit = raycast(world.blocks, worldPos, worldDir)
        .filter { it.position.y < sliceHeight }
        .filter { it.obj.type != BlockAir }
        .firstOrNull()
    position = raycastHit?.let { raycastHit.position + raycastHit.faces.first() }
  }

  override fun area(world: World): Pair<Vector3i, Vector3i>? {
    return if (firstBlock != null && position != null) {
      val secondBlock = position!!
      val xRange = min(firstBlock!!.x, secondBlock.x)..max(firstBlock!!.x, secondBlock.x)
      val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
      val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)
      Vector3i(xRange.start, yRange.start, zRange.start) to Vector3i(xRange.endInclusive + 1, yRange.endInclusive + 1, zRange.endInclusive + 1)
    } else {
      super.area(world)
    }
  }

  override fun areaColor(world: World): Color4 = Color4(255, 255, 255, 64)
}
