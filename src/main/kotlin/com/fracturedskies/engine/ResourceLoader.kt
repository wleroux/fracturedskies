package com.fracturedskies.engine

import org.lwjgl.BufferUtils
import java.nio.ByteBuffer
import java.nio.channels.Channels

fun loadByteBuffer(name: String, classLoader: ClassLoader): ByteBuffer {
  classLoader.getResourceAsStream(name).use { imageStream ->
    requireNotNull(imageStream)
    Channels.newChannel(imageStream).use { channel ->
      var buffer = BufferUtils.createByteBuffer(8)
      while (true) {
        val read = channel.read(buffer)
        if (read == -1)
          break
        if (buffer.remaining() == 0)
          buffer = buffer.expand()
      }
      buffer.flip()
      return buffer
    }
  }
}

private fun ByteBuffer.expand(): ByteBuffer {
  val newBuffer = BufferUtils.createByteBuffer(this.capacity() * 2)
  this.flip()
  newBuffer.put(this)
  return newBuffer
}