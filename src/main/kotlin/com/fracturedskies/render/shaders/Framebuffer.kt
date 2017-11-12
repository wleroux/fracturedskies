package com.fracturedskies.render.shaders

import org.lwjgl.opengl.GL20.glDrawBuffers
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32.glFramebufferTexture

class Framebuffer {
  val id = GL30.glGenFramebuffers()

  fun renderbuffer(attachment: Int, renderbuffer: Renderbuffer) {
    bind {
      glFramebufferRenderbuffer(GL_FRAMEBUFFER, attachment, GL_RENDERBUFFER, renderbuffer.id)
    }
  }

  fun texture(attachment: Int, texture: Texture) {
    bind {
      glFramebufferTexture(GL_FRAMEBUFFER, attachment, texture.id, 0)
    }
  }

  fun drawBuffers(vararg attachments: Int) {
    bind {
      glDrawBuffers(attachments)
    }
  }

  fun bind(block: ()->Unit) {
    glBindFramebuffer(GL_FRAMEBUFFER, id)
    try {
      block()
    } finally {
      glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }
  }

  fun close() {
    glDeleteFramebuffers(id)
  }
}