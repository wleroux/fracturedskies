#version 450 core

in vec3 TexCoord;
in vec4 Color;
out vec4 color;
layout (location = 3) uniform sampler2D albedo;

void main()
{
    color = vec4(Color.rgb / 255, texture2D(albedo, TexCoord.xy).r);
}
