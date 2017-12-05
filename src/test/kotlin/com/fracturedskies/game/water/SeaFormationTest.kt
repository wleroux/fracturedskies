package com.fracturedskies.game.water

import com.fracturedskies.engine.collections.Dimension
import com.fracturedskies.engine.math.Vector3i
import org.junit.Assert.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SeaFormationTest {

  private val xRange = (0 until 10)
  private val yRange = (0 until 10)
  private val zRange = (0 until 10)
  private val water = WaterMap(Dimension(10, 10, 10))

  @Test
  fun itStartsWithNoSeas() {
    assertEquals(0, seas().size)
  }

  @Test
  fun itCanContainSingleSea() {
    set(1, 1, 1, 1)
    assertEquals(get(1, 1, 1), 1)
    assertTrue(disturbed(1, 1, 1))

    val sea = seaAt(1, 1, 1)
    assertNotNull("sea should be created", sea)
    for (neighborPos in shore(1, 1, 1)) {
      assertEquals(sea, seaAt(neighborPos), "neighbor's should share the same sea")
    }
    assertEquals(1, seas().size)
    assertEquals(sea!!.providence.size, blocksWithSeas())
  }

  @Test
  fun itCanContainMultipleSeas() {
    set(1, 1, 1, 1)
    undisturb(1, 1, 1)

    set(8, 8, 8, 1)
    assertNotEquals(seaAt(1, 1, 1), seaAt(8, 8, 8), "new seas should be created")
    assertEquals(2, seas().size, "sea count")
    assertEquals(14, blocksWithSeas(), "sea size")
    assertTrue(disturbed(8, 8, 8))
    assertFalse(disturbed(1, 1, 1))
  }


  @Test
  fun itDoesNothingOnNoChange_WithWater() {
    set(1, 1, 1, 1)
    undisturb(1, 1, 1)
    set(1, 1, 1, 1)
    assertFalse(disturbed(1, 1, 1))
  }

  @Test
  fun itDoesNothingOnNoChange_WithoutWater() {
    set(1, 1, 1, 1)
    undisturb(1, 1, 1)

    set(2, 1, 1, 0)
    set(3, 1, 1, 0)
    assertFalse(disturbed(1, 1, 1))
  }

  @Test
  fun itCombinesSeas_AdjacentSea() {
    set(1, 1, 1, 1)
    undisturb(1, 1, 1)

    set(3, 1, 1, 1)
    assertEquals(seaAt(1, 1, 1), seaAt(3, 1, 1), "new seas should be merged")
    assertEquals(1, seas().size)
    assertEquals(7+7-1, blocksWithSeas())
    assertTrue(disturbed(1, 1, 1))
  }

  @Test
  fun itCombinesSeas_MultipleSeas() {
    set(1, 1, 1, 1)
    set(5, 1, 1, 1)
    set(3, 1, 1, 1)

    assertEquals(seaAt(3, 1, 1), seaAt(1, 1, 1))
    assertEquals(seaAt(3, 1, 1), seaAt(5, 1, 1))
    assertEquals(1, seas().size)
    assertEquals(7+7+7-2, blocksWithSeas())
    assertEquals(7+7+7-2, seaAt(1, 1, 1)!!.providence.size)
  }

  @Test
  fun itCombinesSeas_RemovingOpaqueBlocks() {
    set(1, 1, 1, 1)
    block(2, 1, 1)
    set(3, 1, 1, 1)
    undisturb(1, 1, 1)
    undisturb(3, 1, 1)

    assertNotEquals(seaAt(1, 1, 1), seaAt(3, 1, 1))
    assertEquals(2, seas().size)

    unblock(2, 1, 1)
    assertEquals(seaAt(1, 1, 1), seaAt(3, 1, 1))
    assertTrue(disturbed(2, 1, 1))
  }

  @Test
  fun itCombinesSeas_RemovingOpaqueBlocks_Adjacent() {
    set(1, 1, 1, 1)
    block(2, 1, 1)
    undisturb(1, 1, 1)

    assertEquals(1, seas().size)
    unblock(2, 1, 1)
    assertEquals(seaAt(1, 1, 1), seaAt(2, 1, 1))
    assertTrue(disturbed(2, 1, 1))
  }

  @Test
  fun itSplitsSeas() {
    set(1, 1, 1, 1)
    set(5, 1, 1, 1)
    set(3, 1, 1, 1)

    set(3, 1, 1, 0)
    split(seaAt(1, 1, 1)!!)

    assertNotEquals(seaAt(1, 1, 1), seaAt(5, 1, 1))
    assertEquals(2, seas().size)
    assertEquals(7+7, blocksWithSeas())
  }

  @Test
  fun itSplitsSeas_WithOverlap() {
    // Set Joining Block
    set(3, 3, 3, 1)

    // Sea 1
    set(1, 3, 3, 1)
    set(1, 3, 1, 1)
    set(3, 3, 1, 1)

    // Sea 2
    set(5, 3, 3, 1)
    set(5, 3, 5, 1)
    set(3, 3, 5, 1)

    // Sea 3
    set(3, 5, 3, 1)

    // Sea 4
    set(3, 1, 3, 1)

    // remove joining block
    assertEquals(1, seas().size)
    set(3, 3, 3, 0)
    water.splitSea(seaAt(1, 3, 3)!!)

    assertNotEquals(seaAt(1, 3, 3), seaAt(5, 3, 3))
    assertEquals(6-2, seas().size)
    assertEquals(7+7+7+7+7+7 + 7-2 + 7-2, blocksWithSeas())
  }

  @Test
  fun itSplitsSeas_AddingOpaqueBlocks() {
    set(1, 1, 1, 1)
    undisturb(1, 1, 1)
    set(3, 1, 1, 1)
    undisturb(3, 1, 1)

    block(2, 1, 1)
    water.splitSea(seaAt(1, 1, 1)!!)

    assertNull(seaAt(2, 1, 1))
    assertNotEquals(seaAt(1, 1, 1), seaAt(3, 1, 1))
    assertEquals(2, seas().size)
    assertFalse(disturbed(1, 1, 1))
    assertFalse(disturbed(3, 1, 1))
  }

  @Test
  fun itSplitsSeas_NoSeaChange() {
    set(1, 1, 1, 1)
    set(2, 1, 1, 1)
    set(4, 1, 1, 1)

    assertEquals(seaAt(1, 1, 1), seaAt(2, 1, 1))
    assertEquals(seaAt(1, 1, 1), seaAt(4, 1, 1))
    undisturb(2, 1, 1)

    set(2, 1, 1, 0)
    split(seaAt(2, 1, 1)!!)
    assertEquals(seaAt(1, 1, 1), seaAt(2, 1, 1))
    assertNotEquals(seaAt(1, 1, 1), seaAt(4, 1, 1))
    assertTrue(disturbed(2, 1, 1))
  }

  /* ShortHands */
  private fun block(x: Int, y: Int, z: Int) = water.setOpaque(Vector3i(x, y, z), true)
  private fun unblock(x: Int, y: Int, z: Int) = water.setOpaque(Vector3i(x, y, z), false)
  private fun shore(x: Int, y: Int, z: Int): List<Vector3i> = water.shore(Vector3i(x, y, z))
  private fun seaAt(pos: Vector3i) = seaAt(pos.x, pos.y, pos.z)
  private fun seaAt(x: Int, y: Int, z: Int): WaterMap.Sea? = water.sea[x, y, z]
  private fun disturbed(x: Int, y: Int, z: Int) = seaAt(x, y, z)!!.disturbed
  private fun undisturb(x: Int, y: Int, z: Int) { seaAt(x, y, z)!!.disturbed = false}
  private fun get(x: Int, y: Int, z: Int) = water.getLevel(Vector3i(x, y, z)).toInt()
  private fun set(x: Int, y: Int, z: Int, value: Int) = water.setLevel(Vector3i(x, y, z), value.toByte())
  private fun seas() = water.seas
  private fun split(sea: WaterMap.Sea) = water.splitSea(sea)

  /* Helpers */
  private fun blocksWithSeas(): Int {
    var count = 0
    for (x in xRange) {
      for (y in yRange) {
        for (z in zRange) {
          water.sea[x, y, z]?.let { count ++ }
        }
      }
    }
    return count
  }
}