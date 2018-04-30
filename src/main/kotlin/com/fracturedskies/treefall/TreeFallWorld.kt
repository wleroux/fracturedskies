package com.fracturedskies.treefall

import com.fracturedskies.api.*
import com.fracturedskies.api.block.*
import com.fracturedskies.engine.Id
import com.fracturedskies.engine.api.Cause
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.engine.math.PathFinder.Companion.target
import javax.enterprise.event.Observes
import javax.inject.*


@Singleton
class TreeFallSystem {

  @Inject
  lateinit var world: World

  private data class Tree(val rootPos: Vector3i, val nodes: MutableList<Vector3i>)

  private var initialized = false
  private lateinit var treeSpace: ObjectMutableSpace<Tree?>
  private val treePathFinder = PathFinder({ fromPos, toPos ->
    val fromTree = treeSpace[fromPos]
    val fromBlockType = world.blocks[fromPos].type
    val toBlockType = world.blocks[toPos].type
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
    val block = world.blocks[pos]
    block.type == BlockDirt || block.type == BlockGrass
  }
  private fun treeCostHeuristic(): CostHeuristic = { cost: Int, pos: Vector3i ->
    cost + when (world.blocks[pos].type) {
      BlockDirt -> pos.y
      BlockGrass -> pos.y
      BlockWood -> pos.y
      else -> world.height
    }
  }

  fun onWorldGenerated(@Observes worldGenerated: WorldGenerated) {
    if (!initialized) {
      treeSpace = ObjectMutableSpace(world.dimension, { null })
      initialized = true
    }

    worldGenerated.blocks.forEach { (blockPos, block) -> checkTree(blockPos, block.type) }
  }

  fun onBlocksUpdated(@Observes blocksUpdated: BlocksUpdated) {
    if (!initialized) {
      treeSpace = ObjectMutableSpace(world.dimension, { null })
      initialized = true
    }

    blocksUpdated.blocks.forEach { blockPos, block -> checkTree(blockPos, block.type) }
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
          val nodeBlockType = world.blocks[node].type
          if (nodeBlockType == BlockWood || nodeBlockType == BlockLeaves) {
            val pathToRoot = treePathFinder.find(node, target(preExistingTree.rootPos), treeCostHeuristic()).path
            pathToRoot.isEmpty()
          } else {
            false
          }
        }

        val drops = decayNodes.flatMap {
          val itemDrop = world.blocks[it].type.itemDrop
          if (itemDrop != null) listOf(it to itemDrop) else emptyList()
        }.toMap()
        val blockUpdates = decayNodes.map { it to Block(BlockAir) }.toMap()

        world.updateBlocks(blockUpdates, Cause.of(this))
        drops.forEach { dropPos, itemType ->
          world.spawnItem(Id(), itemType, dropPos, Cause.of(this))
        }
      }
    }
  }

  private fun remove(pos: Vector3i) {
    val blockType = world.blocks[pos].type
    val itemDrop = blockType.itemDrop

    world.updateBlock(pos, Block(BlockAir), Cause.of(this))
    if (itemDrop != null)
      world.spawnItem(Id(), itemDrop, pos, Cause.of(this))
  }
}