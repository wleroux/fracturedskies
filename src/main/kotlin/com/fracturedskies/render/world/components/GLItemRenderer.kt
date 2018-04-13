package com.fracturedskies.render.world.components

import com.fracturedskies.api.BlockType
import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.Vector3
import com.fracturedskies.render.common.components.gl.GLMeshRenderer.Companion.meshRenderer
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.world.WorldState.Item


class GLItemRenderer(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.itemRenderer(item: Item, skyLight: Int, blockLight: Int, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::GLItemRenderer, MultiTypeMap(
          ITEM to item,
          SKY_LIGHT to skyLight,
          BLOCK_LIGHT to blockLight
      ).with(additionalProps)))
    }

    val ITEM = TypedKey<Item>("item")
    val SKY_LIGHT = TypedKey<Int>("skyLight")
    val BLOCK_LIGHT = TypedKey<Int>("blockLight")
  }

  var cache = mutableMapOf<BlockType, MutableMap<Pair<Int, Int>, Mesh>>()
  var mesh: Mesh? = null
  override fun componentWillUpdate(nextProps: MultiTypeMap, nextState: Unit) {
    super.componentWillUpdate(nextProps, nextState)

    val blockType = nextProps[ITEM].blockType
    val skyLight = nextProps[SKY_LIGHT]
    val blockLight = nextProps[BLOCK_LIGHT]
    mesh = cache
        .computeIfAbsent( blockType, { mutableMapOf() })
        .computeIfAbsent( skyLight to blockLight, {
          generateBlock(blockType.color, skyLight.toFloat(), blockLight.toFloat(),
              Vector3(0.25f, 0.00f, 0.25f),
              Vector3(0.50f, 0.50f, 0.50f)
          ).invoke()
        })
  }

  override fun render() = nodes {
    meshRenderer(this@GLItemRenderer.mesh!!)
  }
}