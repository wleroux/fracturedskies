#version 450 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 texCoord;

layout (location = 0) uniform mat4 model;
layout (location = 2) uniform mat4 projection;

out vec3 TexCoord;

void main()
{
    gl_Position = projection * model * vec4(position, 1.0f);
    TexCoord = texCoord;
}