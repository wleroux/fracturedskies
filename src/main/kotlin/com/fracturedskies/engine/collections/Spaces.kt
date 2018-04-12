package com.fracturedskies.engine.collections

import com.fracturedskies.api.CHUNK_DIMENSION
import com.fracturedskies.engine.math.Vector3i
import org.lwjgl.BufferUtils
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

interface Space<K> {
  val dimension: Dimension
  val width get() = dimension.width
  val height get() = dimension.height
  val depth get() = dimension.depth
  operator fun get(index: Int): K
  operator fun get(pos: Vector3i) = get(dimension(pos))
  operator fun get(x: Int, y: Int, z: Int) = get(dimension(x, y, z))
  fun has(index: Int) = dimension.has(index)
  fun has(pos: Vector3i) = has(pos.x, pos.y, pos.z)
  fun has(x: Int, y: Int, z: Int) = dimension.has(x, y, z)
  fun mutate(mutator: (MutableSpace<K>.() -> Unit)): Space<K>
}
fun <K, T> Space<K>.map(mapper: (K) -> T): Space<T> {
  return object : Space<T> {
    override val dimension: Dimension get() = this@map.dimension
    override fun get(index: Int): T = mapper(this@map[index])
    override fun mutate(mutator: MutableSpace<T>.() -> Unit): Space<T> {
      val mutations = SpaceMutator(this).apply(mutator)
      return ObjectSpace(dimension, { mutations[it] })
    }
  }
}
interface MutableSpace<K>: Space<K> {
  operator fun set(index: Int, value: K)
  operator fun set(pos: Vector3i, value: K) = set(dimension(pos), value)
  operator fun set(x: Int, y: Int, z: Int, value: K) = set(dimension(x, y, z), value)
}

inline fun <K> Space<K>.forEach(block: (Vector3i, K) -> Unit) {
  this.dimension.indices().forEach {
    block(dimension.toVector3i(it), this[it])
  }
}

open class BooleanSpace(final override val dimension: Dimension, init: (Int) -> Boolean = { false }): Space<Boolean> {
  protected val backend = BitSet(dimension.size).apply {
    dimension.forEach { index -> set(index, init(index)) }
  }
  override fun get(index: Int) = backend[index]
  override fun mutate(mutator: MutableSpace<Boolean>.() -> Unit): BooleanSpace {
    val mutations = SpaceMutator(this).apply(mutator)
    return BooleanSpace(dimension, { mutations[it] })
  }
}
open class BooleanMutableSpace(dimension: Dimension, init: (Int) -> Boolean = { false }): BooleanSpace(dimension, init), MutableSpace<Boolean> {
  override operator fun set(index: Int, value: Boolean) { backend[index] = value }
  override fun mutate(mutator: MutableSpace<Boolean>.() -> Unit): BooleanSpace = apply(mutator)
}

open class ByteSpace(final override val dimension: Dimension, init: (Int) -> Byte = { 0.toByte() }): Space<Byte> {
  protected val backend = requireNotNull(BufferUtils.createByteBuffer(dimension.size).apply {
    dimension.forEach { index -> put(index, init(index) ) }
  })
  override fun get(index: Int) = backend[index]
  override fun mutate(mutator: MutableSpace<Byte>.() -> Unit): ByteSpace {
    val mutations = SpaceMutator(this).apply(mutator)
    return ByteSpace(dimension, { mutations[it] })
  }
}
open class ByteMutableSpace(dimension: Dimension, init: (Int) -> Byte = { 0.toByte() }): ByteSpace(dimension, init), MutableSpace<Byte> {
  override operator fun set(index: Int, value: Byte) { backend.put(index, value) }
  fun clear() { BufferUtils.zeroBuffer(backend) }
  override fun mutate(mutator: MutableSpace<Byte>.() -> Unit): ByteMutableSpace = apply(mutator)
}

open class IntSpace(final override val dimension: Dimension, init: (Int) -> Int = { 0 }): Space<Int> {
  protected val backend = IntArray(dimension.size, init)
  override fun get(index: Int) = backend[index]
  override fun mutate(mutator: MutableSpace<Int>.() -> Unit): IntSpace {
    val mutations = SpaceMutator(this).apply(mutator)
    return IntSpace(dimension, { mutations[it] })
  }
}
open class IntMutableSpace(dimension: Dimension, init: (Int) -> Int = { 0 }): IntSpace(dimension, init), MutableSpace<Int> {
  override operator fun set(index: Int, value: Int) { backend[index] = value }
  override fun mutate(mutator: MutableSpace<Int>.() -> Unit): IntMutableSpace = apply(mutator)
}

open class ChunkSpace<K>(final override val dimension: Dimension, init: (Int) -> Space<K>): Space<K> {
  private val chunkDimension = dimension / CHUNK_DIMENSION
  val chunks = ObjectSpace(chunkDimension, { init(it) })

  override fun get(index: Int): K {
    val spacePos = dimension.toVector3i(index)
    val chunkPos = spacePos / CHUNK_DIMENSION
    val localPos = spacePos % CHUNK_DIMENSION
    return chunks[chunkPos][localPos]
  }

  override fun mutate(mutator: MutableSpace<K>.() -> Unit): ChunkSpace<K> {
    val mutatedSpace = SpaceMutator(this).apply(mutator)
    val mutatedChunks = mutatedSpace.mutations.entries
        .groupBy { dimension.toVector3i(it.key) / CHUNK_DIMENSION }

    return ChunkSpace(dimension, { chunkIndex ->
      val chunkPos = chunkDimension.toVector3i(chunkIndex)
      if (!mutatedChunks.containsKey(chunkPos)) {
        chunks[chunkIndex]
      } else {
        chunks[chunkIndex].mutate {
          mutatedChunks[chunkPos]!!.forEach { (index, value) ->
            val spacePos = this@ChunkSpace.dimension.toVector3i(index)
            val localPos = spacePos % CHUNK_DIMENSION
            set(localPos, value)
          }
        }
      }
    })
  }
}

open class ObjectSpace<K>(final override val dimension: Dimension, init: (Int) -> K): Space<K> {
  @Suppress("UNCHECKED_CAST")
  protected val backend = Array(dimension.size, { init(it) as Any? }) as Array<K>

  @Suppress("UNCHECKED_CAST")
  override operator fun get(index: Int) = backend[index]
  override fun mutate(mutator: (MutableSpace<K>.() -> Unit)): ObjectSpace<K> {
    val mutatedSpace = SpaceMutator(this).apply(mutator)
    return ObjectSpace(dimension, { mutatedSpace[it] })
  }
}
open class ObjectMutableSpace<K>(dimension: Dimension, init: (Int) -> K): ObjectSpace<K>(dimension, init), MutableSpace<K> {
  override operator fun set(index: Int, value: K) { backend[index] = value }
}

private class SpaceMutator<K>(private val backend: Space<K>) : MutableSpace<K> {
  override val dimension = backend.dimension
  val mutations = hashMapOf<Int, K>()
  override fun get(index: Int): K {
    @Suppress("UNCHECKED_CAST")
    return if (mutations.containsKey(index)) mutations[index] as K else backend[index]
  }

  override fun set(index: Int, value: K) {
    mutations[index] = value
  }
  override fun mutate(mutator: MutableSpace<K>.() -> Unit) = apply(mutator)
}