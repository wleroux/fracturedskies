#version 450 core

out vec4 color;

in vec3 TexCoord;

layout (location = 3) uniform sampler2DArray albedo;
layout (location = 4) uniform vec4 texColor;


void main()
{
    color = texColor * texture(albedo, TexCoord);
}
