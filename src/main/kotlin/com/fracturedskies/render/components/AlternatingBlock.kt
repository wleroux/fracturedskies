package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.event.on
import com.fracturedskies.engine.jeact.nodes
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.components.MeshRenderer.Companion.meshRenderer
import com.fracturedskies.render.events.Click
import com.fracturedskies.render.mesh.Material
import com.fracturedskies.render.mesh.Mesh
import com.fracturedskies.render.mesh.TextureArray
import com.fracturedskies.render.mesh.standard.StandardShaderProgram
import org.lwjgl.glfw.GLFW.GLFW_RELEASE

class AlternatingBlock(attributes: Context) : AbstractComponent<Int>(attributes, 0) {
  companion object {
    fun Node.Builder<*>.alternatingBlock(additionalContext: Context = Context()) {
      nodes.add(Node(::AlternatingBlock, additionalContext))
    }
  }

  /* State */
  private var blockType
    get() = (nextState ?: state)
    set(value) {nextState = value}

  override fun preferredWidth(parentWidth: Int, parentHeight: Int) = 100
  override fun preferredHeight(parentWidth: Int, parentHeight: Int) = 100
  override fun toNode(): List<Node<*>> {
    val variables = Context(
      StandardShaderProgram.MODEL to Matrix4.IDENTITY,
      StandardShaderProgram.VIEW to Matrix4.IDENTITY,
      StandardShaderProgram.PROJECTION to Matrix4.IDENTITY
    )
    val material = Material(
            StandardShaderProgram(),
            Context(StandardShaderProgram.ALBEDO to TextureArray("tileset.png", loadByteBuffer("com/fracturedskies/render/tileset.png", this.javaClass.classLoader), 16, 16, 3))
    )
    val mesh = Mesh(floatArrayOf(
      -1f, -1f, 0f,   0f, 0f, blockType.toFloat(),   0f, 0f, 1f,
      -1f,  1f, 0f,   0f, 1f, blockType.toFloat(),   0f, 0f, 1f,
       1f,  1f, 0f,   1f, 1f, blockType.toFloat(),   0f, 0f, 1f,
       1f, -1f, 0f,   1f, 0f, blockType.toFloat(),   0f, 0f, 1f
    ), intArrayOf(
      0, 1, 2,
      2, 3, 0
    ), listOf(Mesh.Attribute.POSITION, Mesh.Attribute.TEXCOORD, Mesh.Attribute.NORMAL))

    return nodes {
      meshRenderer(mesh, material, variables)
    }
  }

  override val handler = on(Click::class) {
    if (it.action == GLFW_RELEASE) {
      nextBlock()
    }
  }

  private fun nextBlock() {
    blockType = (blockType + 1) % 3
  }
}