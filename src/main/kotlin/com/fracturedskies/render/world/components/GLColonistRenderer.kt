package com.fracturedskies.render.world.components

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.common.components.gl.GLMeshRenderer.Companion.meshRenderer
import com.fracturedskies.render.common.shaders.Mesh
import com.fracturedskies.render.world.Worker


class GLColonistRenderer(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.colonistRenderer(colonist: Worker, skyLight: Int, blockLight: Int, additionalProps: MultiTypeMap = MultiTypeMap()) {
      nodes.add(Node(::GLColonistRenderer, MultiTypeMap(
          COLONIST to colonist,
          SKY_LIGHT to skyLight,
          BLOCK_LIGHT to blockLight
      ).with(additionalProps)))
    }

    val SKY_LIGHT = TypedKey<Int>("skyLight")
    val BLOCK_LIGHT = TypedKey<Int>("blockLight")
    val COLONIST = TypedKey<Worker>("colonist")
  }

  var cache = mutableMapOf<Pair<Int, Int>, Mesh>()
  var mesh: Mesh? = null
  override fun componentWillUpdate(nextProps: MultiTypeMap, nextState: Unit) {
    super.componentWillUpdate(nextProps, nextState)

    val skyLight = nextProps[SKY_LIGHT]
    val blockLight = nextProps[BLOCK_LIGHT]
    mesh = cache.computeIfAbsent(skyLight to blockLight, { _ ->
      generateBlock(Color4.WHITE, skyLight.toFloat(), blockLight.toFloat(),
          Vector3(0.25f, 0.00f, 0.25f),
          Vector3(0.50f, 0.50f, 0.50f)
      ).invoke()
    })
  }

  override fun render() = nodes {
    meshRenderer(this@GLColonistRenderer.mesh!!)
  }
}