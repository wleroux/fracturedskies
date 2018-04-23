package com.fracturedskies.treefall

import com.fracturedskies.WorldState
import com.fracturedskies.api.*
import com.fracturedskies.api.BlockType.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.PathFinder.Companion.target
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send


class TreeFallWorldState(dimension: Dimension) : WorldState(dimension) {
  private data class Tree(val rootPos: Vector3i, val nodes: MutableList<Vector3i>)
  private val treeSpace = ObjectMutableSpace<Tree?>(dimension, { null })
  private val treePathFinder = PathFinder({ fromPos, toPos ->
    val fromTree = treeSpace[fromPos]
    val fromBlockType = blocks[fromPos].type
    val toBlockType = blocks[toPos].type
    val toTree = treeSpace[toPos]
    when {
      fromTree !== null && fromTree !== toTree -> false
      fromBlockType == LEAVE && toBlockType == LEAVE -> true
      fromBlockType == LEAVE && toBlockType == WOOD -> true
      fromBlockType == WOOD && toBlockType == WOOD -> true
      fromBlockType == WOOD && toBlockType == DIRT -> true
      fromBlockType == WOOD && toBlockType == GRASS -> true
      else -> false
    }
  })
  private fun isTreeRoot(): IsTarget = {pos: Vector3i ->
    val block = blocks[pos]
    block.type == DIRT || block.type == GRASS
  }
  private fun treeCostHeuristic(): CostHeuristic = { pos: Vector3i ->
    when (blocks[pos].type) {
      DIRT -> pos.y
      GRASS -> pos.y
      WOOD -> pos.y
      else -> dimension.height
    }
  }

  override fun process(message: Any) {
    super.process(message)
    when (message) {
      is WorldGenerated -> message.blocks.forEach { (blockIndex, block) ->
        checkTree(dimension.toVector3i(blockIndex), block.type)
      }
      is BlockUpdated -> message.updates.forEach { blockIndex, blockType ->
        checkTree(blockIndex, blockType)
      }
    }
  }

  private fun checkTree(pos: Vector3i, blockType: BlockType) {
    val preExistingTree = treeSpace[pos]
    if (preExistingTree == null) {
      if (blockType == LEAVE || blockType == WOOD) {
        val treeRootPath = treePathFinder.find(pos, isTreeRoot(), treeCostHeuristic())
        if (treeRootPath.isNotEmpty()) {
          val treeRootPos = treeRootPath.last()
          val tree = treeSpace[treeRootPos] ?: Tree(treeRootPos, mutableListOf())
          treeRootPath.forEach {
            tree.nodes += it
            treeSpace[it] = tree
          }
        } else {
          // Created with no root
          remove(pos)
        }
      }
    } else {
      val isRoot = preExistingTree.rootPos == pos
      if (isRoot && (blockType == DIRT || blockType == GRASS)) {
        // Nothing to be concerned about.
      } else if (blockType == LEAVE || blockType == WOOD) {
        // Nothing to be concerned about.
      } else {
        val decayNodes = preExistingTree.nodes.filter { node ->
          val nodeBlockType = blocks[node].type
          if (nodeBlockType == WOOD || nodeBlockType == LEAVE) {
            val pathToRoot = treePathFinder.find(node, target(preExistingTree.rootPos), treeCostHeuristic())
            pathToRoot.isEmpty()
          } else {
            false
          }
        }

        val drops = decayNodes.flatMap {
          val blockDrop = blocks[it].type.blockDrop
          if (blockDrop != null) listOf(it to blockDrop) else emptyList()
        }.toMap()
        val blockUpdates = decayNodes.map { it to AIR }.toMap()
        send(BlockUpdated(blockUpdates, Cause.of(this)))
        drops.forEach { dropPos, dropBlockType ->
          send(ItemSpawned(Id(), dropBlockType, dropPos, Cause.of(this)))
        }
      }
    }
  }

  private fun remove(pos: Vector3i) {
    val blockType = blocks[pos].type
    val blockDrop = blockType.blockDrop
    send(BlockUpdated(mapOf(pos to AIR), Cause.of(this)))
    if (blockDrop != null)
      send(ItemSpawned(Id(), blockDrop, pos, Cause.of(this)))
  }
}