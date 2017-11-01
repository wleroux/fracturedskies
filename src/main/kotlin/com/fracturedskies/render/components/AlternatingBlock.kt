package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.TypedMap
import com.fracturedskies.engine.jeact.Component
import com.fracturedskies.engine.jeact.VNode
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.mesh.Material
import com.fracturedskies.render.mesh.Mesh
import com.fracturedskies.render.mesh.TextureArray
import com.fracturedskies.render.mesh.standard.StandardShaderProgram
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class AlternatingBlock(override var attributes: TypedMap) : Component {
  override var state: Any? = null
  override var nextState: Any? = null
  var blockType: Int
    get() = state as Int
    set(value) {
      nextState = value
    }
  init {
    blockType = 0
  }

  lateinit var job: Job
  override fun didMount() {
    super.didMount()
    job = launch {
      while(isActive) {
        blockType = (blockType + 1) % 3
        delay(1000)
      }
    }
  }

  override fun willUnmount() {
    super.willUnmount()
    job.cancel()
  }

  override fun children(): List<VNode> {
    val variables = TypedMap(
      StandardShaderProgram.MODEL to Matrix4(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
      ),
      StandardShaderProgram.VIEW to Matrix4(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
      ).invert(),
      StandardShaderProgram.PROJECTION to Matrix4(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
      )
    )
    val material = Material(
            StandardShaderProgram(),
            TypedMap(StandardShaderProgram.ALBEDO to TextureArray("tileset.png", loadByteBuffer("com/fracturedskies/render/tileset.png", this.javaClass.classLoader), 16, 16, 3))
    )
    val mesh = Mesh(floatArrayOf(
      -0.5f, -0.5f, 0f,   0f, 0f, blockType.toFloat(),   0f, 0f, 1f,
      -0.5f,  0.5f, 0f,   0f, 1f, blockType.toFloat(),   0f, 0f, 1f,
       0.5f,  0.5f, 0f,   1f, 1f, blockType.toFloat(),   0f, 0f, 1f,
       0.5f, -0.5f, 0f,   1f, 0f, blockType.toFloat(),   0f, 0f, 1f
    ), intArrayOf(
      0, 1, 2,
      2, 3, 0
    ), listOf(Mesh.Attribute.POSITION, Mesh.Attribute.TEXCOORD, Mesh.Attribute.NORMAL))

    return listOf(VNode(::MeshRenderer, MeshRenderer.MESH to mesh, MeshRenderer.MATERIAL to material, MeshRenderer.VARIABLES to variables))
  }
}