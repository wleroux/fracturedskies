package com.fracturedskies.render.world.controller

import com.fracturedskies.WorldState
import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.AIR
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.Vector3i.Companion.area
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send
import com.fracturedskies.task.api.*
import com.fracturedskies.water.api.MAX_WATER_LEVEL
import org.lwjgl.glfw.GLFW.*
import java.lang.Integer.*

// Actions
data class WorldMouseClick(val world: WorldState, val worldPos: Vector3, val worldDir: Vector3, val action: Int, val button: Int, val mods: Int)
data class WorldMouseMove(val world: WorldState, val worldPos: Vector3, val worldDir: Vector3)
interface WorldActionController {
  fun onClick(worldMouseClick: WorldMouseClick, sliceHeight: Int) = Unit
  fun onMove(worldMouseMove: WorldMouseMove, sliceHeight: Int) = Unit
  fun onUpdate(world: WorldState, dt: Float) = Unit
}

object NoopActionController: WorldActionController

// Spawn colonist
object SpawnColonistActionController : WorldActionController {
  override fun onClick(worldMouseClick: WorldMouseClick, sliceHeight: Int) {
    val (world, worldPos, worldDir, action, _, _) = worldMouseClick
    if (action == GLFW_RELEASE) {
      val raycastHit = raycast(world.blocks, worldPos, worldDir)
          .filter { it.position.y < sliceHeight }
          .filter { it.obj.type != AIR }
          .firstOrNull()
      if (raycastHit != null) {
        val pos = raycastHit.position + raycastHit.faces.first()
        if (world.dimension.has(pos) && world.blocks[pos].type == AIR)
          send(ColonistSpawned(Id(), pos, Cause.of(this)))
      }
    }
  }
}

// Add Block
class AddBlockActionController(val blockType: BlockType): WorldActionController {
  var firstBlock: Vector3i? = null
  override fun onClick(worldMouseClick: WorldMouseClick, sliceHeight: Int) {
    val (world, worldPos, worldDir, action, _, _) = worldMouseClick
    val raycastHit = raycast(world.blocks, worldPos, worldDir)
        .filter { it.position.y < sliceHeight }
        .filter { it.obj.type != AIR }
        .firstOrNull()
    if (raycastHit == null) {
      firstBlock = null
    } else if (action == GLFW_PRESS) {
      firstBlock = raycastHit.position + raycastHit.faces.first()
    } else if (firstBlock != null && action == GLFW_RELEASE) {
      val secondBlock = raycastHit.position + raycastHit.faces.first()
      val xRange = min(firstBlock!!.x, secondBlock.x)..max(firstBlock!!.x, secondBlock.x)
      val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
      val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)
      area(xRange, yRange, zRange)
          .filter { world.blocks.has(it) && world.blocks[it].type == AIR }
          .forEach {
            send(TaskCreated(Id(), TaskCategory.MINE, TaskPriority.AVERAGE, SingleAssigneeCondition, PlaceBlock(it, blockType), Cause.of(this)))
          }
      firstBlock = null
    }
  }
}

// Remove Block
object RemoveBlockBlockActionController: WorldActionController {
  var firstBlock: Vector3i? = null
  override fun onClick(worldMouseClick: WorldMouseClick, sliceHeight: Int) {
    val (world, worldPos, worldDir, action, _, _) = worldMouseClick
    val raycastHit = raycast(world.blocks, worldPos, worldDir)
        .filter { it.position.y < sliceHeight }
        .filter { it.obj.type != AIR }
        .firstOrNull()
    if (raycastHit == null) {
      firstBlock = null
    } else if (action == GLFW_PRESS) {
      firstBlock = raycastHit.position
    } else if (firstBlock != null && action == GLFW_RELEASE) {
      val secondBlock = raycastHit.position
      val xRange = min(firstBlock!!.x, secondBlock.x)..max(firstBlock!!.x, secondBlock.x)
      val yRange = min(firstBlock!!.y, secondBlock.y)..max(firstBlock!!.y, secondBlock.y)
      val zRange = min(firstBlock!!.z, secondBlock.z)..max(firstBlock!!.z, secondBlock.z)
      area(xRange, yRange, zRange)
          .filter { world.blocks.has(it) && world.blocks[it].type != AIR }
          .forEach {
            send(TaskCreated(Id(), TaskCategory.MINE, TaskPriority.AVERAGE, SingleAssigneeCondition, RemoveBlock(it), Cause.of(this)))
          }
      firstBlock = null
    }
  }
}

// Add Water
object AddWaterBlockActionController: WorldActionController {
  var isOn = false
  var position: Vector3i? = null
  override fun onClick(worldMouseClick: WorldMouseClick, sliceHeight: Int) {
    val (world, worldPos, worldDir, action, _, _) = worldMouseClick
    isOn = (action == GLFW_PRESS)
    onMove(WorldMouseMove(world, worldPos, worldDir), sliceHeight)
  }

  override fun onMove(worldMouseMove: WorldMouseMove, sliceHeight: Int) {
    val (world, worldPos, worldDir) = worldMouseMove
    val raycastHit = raycast(world.blocks, worldPos, worldDir)
        .filter { it.position.y < sliceHeight }
        .filter { it.obj.type != AIR }
        .firstOrNull()
    position = raycastHit?.let { raycastHit.position + raycastHit.faces.first() }
  }

  override fun onUpdate(world: WorldState, dt: Float) {
    if (isOn) {
      position?.let { position ->
        if (!world.blocks.has(position) || world.blocks[position].type != AIR) return
        if (world.blocks[position].waterLevel >= MAX_WATER_LEVEL) return
        send(BlockWaterLevelUpdated(mutableMapOf(position to MAX_WATER_LEVEL), Cause.of(this)))
      }
    }
  }
}

