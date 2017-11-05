package com.fracturedskies.render.components

import com.fracturedskies.engine.collections.Context
import com.fracturedskies.engine.collections.Key
import com.fracturedskies.engine.jeact.AbstractComponent
import com.fracturedskies.engine.jeact.Bounds
import com.fracturedskies.engine.jeact.Node
import com.fracturedskies.engine.jeact.event.EventHandlers
import com.fracturedskies.engine.jeact.event.on
import com.fracturedskies.engine.loadByteBuffer
import com.fracturedskies.engine.math.Matrix4
import com.fracturedskies.render.events.Click
import com.fracturedskies.render.events.Hover
import com.fracturedskies.render.events.Unhover
import com.fracturedskies.render.mesh.Material
import com.fracturedskies.render.mesh.Mesh
import com.fracturedskies.render.mesh.TextureArray
import com.fracturedskies.render.mesh.standard.StandardShaderProgram
import org.lwjgl.BufferUtils.createFloatBuffer
import org.lwjgl.BufferUtils.createIntBuffer
import org.lwjgl.opengl.GL11.*

class Button(attributes: Context) : AbstractComponent<Boolean>(attributes, false) {
  companion object {
    val ON_CLICK = Key<(Click) -> Unit>("onClick")
    private val LAYER_WIDTH = 10
    private val LAYER_HEIGHT = 10

    fun Node.Builder<*>.button(onClick: (Click) -> Unit = {}, additionalContext: Context = Context(), block: Node.Builder<*>.()->Unit = {}) {
      nodes.add(Node(::Button, Context(
              ON_CLICK to onClick
      ).with(additionalContext), block))
    }
  }
  var hover: Boolean
    get() = nextState ?: state
    set(value) {nextState = value}
  val onClick get() = requireNotNull(attributes[ON_CLICK])
  override fun preferredWidth(parentWidth: Int, parentHeight: Int) =
          (children.map({it.preferredWidth(parentWidth - 2 * LAYER_WIDTH, parentHeight - 2 * LAYER_HEIGHT)}).max() ?: 0) + 2 * LAYER_WIDTH
  override fun preferredHeight(parentWidth: Int, parentHeight: Int) =
          (children.map({it.preferredHeight(parentWidth - 2 * LAYER_WIDTH, parentHeight - 2 * LAYER_HEIGHT)}).max() ?: 0) + 2 * LAYER_HEIGHT
  override val handler = EventHandlers(on(Hover::class) {message ->
    hover = true
    message.stopPropogation = true
  }, on(Unhover::class) {message ->
    hover = false
    message.stopPropogation = true
  }, on(Click::class) {
    message -> onClick(message)
  })

  lateinit var defaultMaterial: Material
  lateinit var hoverMaterial: Material
  override fun willMount() {
    hoverMaterial = Material(StandardShaderProgram(), Context(
            StandardShaderProgram.ALBEDO to TextureArray("button_hover.png", loadByteBuffer("com/fracturedskies/render/components/button_hover.png", this.javaClass.classLoader), 4, 4, 9)
    ))
    defaultMaterial = Material(StandardShaderProgram(), Context(
            StandardShaderProgram.ALBEDO to TextureArray("button_default.png", loadByteBuffer("com/fracturedskies/render/components/button_default.png", this.javaClass.classLoader), 4, 4, 9)
    ))
  }

  override fun render(bounds: Bounds) {
    this.bounds = bounds
    val fillWidth = bounds.width - 2 * LAYER_WIDTH
    val fillHeight = bounds.height - 2 * LAYER_HEIGHT

    // @formatter:off
    val verticesBuffer = createFloatBuffer(9 * 4 * 9)
    val indicesBuffer = createIntBuffer(6 * 9)
    var vertexCount = 0
    for (i in 0..2) {
      for (j in 0..2) {
        val tile = j * 3 + i
        val xOffset = if (i != 2) if (i == 0) 0 else LAYER_WIDTH else LAYER_WIDTH + fillWidth
        val yOffset = if (j != 2) if (j == 0) LAYER_HEIGHT + fillHeight else LAYER_HEIGHT else 0
        val w = if (i != 2) if (i == 0) LAYER_WIDTH else fillWidth else LAYER_WIDTH
        val h = if (j != 2) if (j == 0) LAYER_HEIGHT else fillHeight else LAYER_HEIGHT

        verticesBuffer.put(floatArrayOf(
                (xOffset + 0).toFloat(), (yOffset + h).toFloat(), 0f, 0f, 0f, tile.toFloat(), 0f, 0f, 1f,
                (xOffset + w).toFloat(), (yOffset + h).toFloat(), 0f, 1f, 0f, tile.toFloat(), 0f, 0f, 1f,
                (xOffset + w).toFloat(), (yOffset + 0).toFloat(), 0f, 1f, 1f, tile.toFloat(), 0f, 0f, 1f,
                (xOffset + 0).toFloat(), (yOffset + 0).toFloat(), 0f, 0f, 1f, tile.toFloat(), 0f, 0f, 1f
        ))
        indicesBuffer.put(intArrayOf(
                vertexCount + 0, vertexCount + 1, vertexCount + 2,
                vertexCount + 2, vertexCount + 3, vertexCount + 0
        ))
        vertexCount += 4
      }
    }
    // @formatter:on
    verticesBuffer.flip()
    val vertices = FloatArray(verticesBuffer.remaining())
    verticesBuffer.get(vertices)

    indicesBuffer.flip()
    val indices = IntArray(indicesBuffer.remaining())
    indicesBuffer.get(indices)

    val material = if (hover) hoverMaterial else defaultMaterial
    val mesh = Mesh(vertices, indices, listOf(Mesh.Attribute.POSITION, Mesh.Attribute.TEXCOORD, Mesh.Attribute.NORMAL))
    val variables = Context(
            StandardShaderProgram.MODEL to Matrix4.IDENTITY,
            StandardShaderProgram.VIEW to Matrix4.IDENTITY,
            StandardShaderProgram.PROJECTION to Matrix4.orthogonal(0f, bounds.width.toFloat(), 0f, bounds.height.toFloat(), -1f, 1000f)
    )

    glClear(GL_DEPTH_BUFFER_BIT)
    glViewport(bounds.x, bounds.y, bounds.width, bounds.height)
    material.render(variables, mesh)

    for (child in children) {
      child.render(Bounds(bounds.x + LAYER_WIDTH, bounds.y + LAYER_HEIGHT, bounds.width - 2 * LAYER_WIDTH, bounds.height - 2 * LAYER_HEIGHT))
    }
  }
}