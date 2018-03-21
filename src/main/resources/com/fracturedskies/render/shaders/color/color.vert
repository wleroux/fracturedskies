#version 450 core

layout (location = 0) in vec3 position;
layout (location = 2) in vec3 normal;
layout (location = 3) in vec4 color;
layout (location = 4) in float occlusion;
layout (location = 5) in float skyLightLevel;
layout (location = 6) in float blockLightLevel;

layout (location = 0) uniform mat4 model;
layout (location = 1) uniform mat4 view;
layout (location = 2) uniform mat4 projection;
layout (location = 4) uniform uvec4 SkyColors[16];
layout (location = 20) uniform uvec4 BlockColors[16];

out vec4 Color;
out vec3 Normal;
out float Occlusion;
out vec3 BlockLightColor;
out vec3 SkyLightColor;

void main()
{
    gl_Position = projection * view * model * vec4(position, 1.0f);
    Color = color;
    Normal = normal;
    Occlusion = occlusion;

    // Block Light Color
    int startBlockLightLevel = int(trunc(blockLightLevel));
    int endBlockLightLevel = startBlockLightLevel + 1;
    float blockAlpha = blockLightLevel - trunc(blockLightLevel);
    vec3 startBlockLightColor = pow(vec3(BlockColors[startBlockLightLevel]) / 255, vec3(2.0));
    vec3 endBlockLightColor = pow(vec3(BlockColors[endBlockLightLevel]) / 255, vec3(2.0));
    BlockLightColor = mix(startBlockLightColor, endBlockLightColor, blockAlpha);

    // Sky Light Color
    int startSkyLightLevel = int(trunc(skyLightLevel));
    int endSkyLightLevel = startSkyLightLevel + 1;
    float skyAlpha = skyLightLevel - trunc(skyLightLevel);
    vec3 startSkyLightColor = pow(vec3(SkyColors[startSkyLightLevel]) / 255, vec3(2.0));
    vec3 endSkyLightColor = pow(vec3(SkyColors[endSkyLightLevel]) / 255, vec3(2.0));
    SkyLightColor = mix(startSkyLightColor, endSkyLightColor, skyAlpha);
}
