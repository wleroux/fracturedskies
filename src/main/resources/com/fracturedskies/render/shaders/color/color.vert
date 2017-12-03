#version 450 core

layout (location = 0) in vec3 position;
layout (location = 2) in vec3 normal;
layout (location = 3) in vec4 color;
layout (location = 4) in float occlusion;
layout (location = 5) in float skyLightLevel;

layout (location = 0) uniform mat4 model;
layout (location = 1) uniform mat4 view;
layout (location = 2) uniform mat4 projection;

out vec4 Color;
out vec3 Normal;
out float Occlusion;
out float SkyLightLevel;

void main()
{
    gl_Position = projection * view * model * vec4(position, 1.0f);
    Color = color;
    Normal = normal;
    Occlusion = occlusion;
    SkyLightLevel = skyLightLevel;
}
