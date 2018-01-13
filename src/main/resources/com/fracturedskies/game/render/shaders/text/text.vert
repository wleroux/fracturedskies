#version 450 core

layout (location = 0) in vec3 position;
layout (location = 3) in vec4 color;

layout (location = 2) uniform mat4 projection;

out vec4 Color;

void main()
{
    gl_Position = projection * vec4(position, 1.0f);
    Color = color;
}
