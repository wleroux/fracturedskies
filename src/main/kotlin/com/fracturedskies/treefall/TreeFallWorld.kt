package com.fracturedskies.treefall

import com.fracturedskies.World
import com.fracturedskies.api.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.PathFinder.Companion.target
import com.fracturedskies.engine.messages.Cause
import com.fracturedskies.engine.messages.MessageBus.send


class TreeFallWorld(dimension: Dimension) : World(dimension) {
  private data class Tree(val rootPos: Vector3i, val nodes: MutableList<Vector3i>)
  private val treeSpace = ObjectMutableSpace<Tree?>(dimension, { null })
  private val treePathFinder = PathFinder({ fromPos, toPos ->
    val fromTree = treeSpace[fromPos]
    val fromBlockType = blocks[fromPos].type
    val toBlockType = blocks[toPos].type
    val toTree = treeSpace[toPos]
    when {
      fromTree !== null && fromTree !== toTree -> false
      fromBlockType == BlockLeaves && toBlockType == BlockLeaves -> true
      fromBlockType == BlockLeaves && toBlockType == BlockWood -> true
      fromBlockType == BlockWood && toBlockType == BlockWood -> true
      fromBlockType == BlockWood && toBlockType == BlockDirt -> true
      fromBlockType == BlockWood && toBlockType == BlockGrass -> true
      else -> false
    }
  })
  private fun isTreeRoot(): IsTarget = {pos: Vector3i ->
    val block = blocks[pos]
    block.type == BlockDirt || block.type == BlockGrass
  }
  private fun treeCostHeuristic(): CostHeuristic = { cost: Int, pos: Vector3i ->
    cost + when (blocks[pos].type) {
      BlockDirt -> pos.y
      BlockGrass -> pos.y
      BlockWood -> pos.y
      else -> dimension.height
    }
  }

  override fun process(message: Any) {
    super.process(message)
    when (message) {
      is WorldGenerated -> message.blocks.forEach { (blockIndex, block) ->
        checkTree(vector3i(blockIndex), block.type)
      }
      is BlockUpdated -> message.updates.forEach { blockIndex, blockType ->
        checkTree(blockIndex, blockType)
      }
    }
  }

  private fun checkTree(pos: Vector3i, blockType: BlockType) {
    val preExistingTree = treeSpace[pos]
    if (preExistingTree == null) {
      if (blockType == BlockLeaves || blockType == BlockWood) {
        val treeRootPath = treePathFinder.find(pos, isTreeRoot(), treeCostHeuristic()).path
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
      if (isRoot && (blockType == BlockDirt || blockType == BlockGrass)) {
        // Nothing to be concerned about.
      } else if (blockType == BlockLeaves || blockType == BlockWood) {
        // Nothing to be concerned about.
      } else {
        val decayNodes = preExistingTree.nodes.filter { node ->
          val nodeBlockType = blocks[node].type
          if (nodeBlockType == BlockWood || nodeBlockType == BlockLeaves) {
            val pathToRoot = treePathFinder.find(node, target(preExistingTree.rootPos), treeCostHeuristic()).path
            pathToRoot.isEmpty()
          } else {
            false
          }
        }

        val drops = decayNodes.flatMap {
          val itemDrop = blocks[it].type.itemDrop
          if (itemDrop != null) listOf(it to itemDrop) else emptyList()
        }.toMap()
        val blockUpdates = decayNodes.map { it to BlockAir }.toMap()
        send(BlockUpdated(blockUpdates, Cause.of(this)))
        drops.forEach { dropPos, itemType ->
          send(ItemSpawned(Id(), itemType, dropPos, Cause.of(this)))
        }
      }
    }
  }

  private fun remove(pos: Vector3i) {
    val blockType = blocks[pos].type
    val itemDrop = blockType.itemDrop
    send(BlockUpdated(mapOf(pos to BlockAir), Cause.of(this)))
    if (itemDrop != null)
      send(ItemSpawned(Id(), itemDrop, pos, Cause.of(this)))
  }
}