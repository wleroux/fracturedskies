package com.fracturedskies.render.common.components.gl

import com.fracturedskies.engine.collections.*
import com.fracturedskies.engine.jeact.*
import com.fracturedskies.engine.jeact.Node.Builder
import com.fracturedskies.engine.math.*
import com.fracturedskies.render.common.shaders.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import java.nio.IntBuffer


class GLUniform(props: MultiTypeMap) : Component<Unit>(props, Unit) {
  companion object {
    fun Node.Builder<*>.uniform(location: Int, value: Any, additionalProps: MultiTypeMap = MultiTypeMap(), block: Builder<Unit>.() -> Unit = {}) {
      nodes.add(Node(::GLUniform, MultiTypeMap(LOCATION to location, VALUE to value).with(additionalProps), block))
    }

    val LOCATION = TypedKey<Int>("location")
    val VALUE = TypedKey<Any>("value")
  }

  override fun shouldComponentUpdate(nextProps: MultiTypeMap, nextState: Unit): Boolean = false

  override fun glRender(bounds: Bounds) {
    val location = props[LOCATION]
    val value = props[VALUE]
    glUniform(location, value)

    super.glRender(bounds)
  }
}

private val mat4Buffer = BufferUtils.createFloatBuffer(16)
fun glUniform(location: Int, value: Any) {
  when (value) {
    is Matrix4 -> {
      value.store(mat4Buffer)
      mat4Buffer.flip()
      glUniformMatrix4fv(location, false, mat4Buffer)
    }
    is IntBuffer -> {
      glUniform4uiv(location, value)
    }
    is Vector3 -> {
      glUniform3f(location, value.x, value.y, value.z)
    }
    is Texture -> {
      glUniform1i(location, 0)
      glActiveTexture(GL_TEXTURE0)
      glBindTexture(GL_TEXTURE_2D, value.id)
    }
    is TextureArray -> {
      glUniform1i(location, 0)
      glActiveTexture(GL_TEXTURE0)
      glBindTexture(GL_TEXTURE_2D_ARRAY, value.id)
    }
    else -> throw IllegalArgumentException("Unknown uniform type")
  }
}