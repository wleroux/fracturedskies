#version 450 core

out vec4 color;
in vec3 TexCoord;
layout (location = 3) uniform sampler2D albedo;

void main() {
    color = texture(albedo, TexCoord.xy);
}
