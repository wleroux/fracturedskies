#version 450 core

out vec4 color;
in vec4 Color;
in vec3 TexCoord;
layout (location = 3) uniform sampler2D albedo;

void main()
{
    color = (Color / 255) * (texture(albedo, TexCoord.xy).rgba / 1);
}
