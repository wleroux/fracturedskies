#version 450 core
out vec4 color;

in vec4 Color;
in vec3 Normal;
in float Occlusion;
flat in int SkyLightLevel;
flat in int BlockLightLevel;

layout (location = 3) uniform vec3 lightDir;
layout (location = 4) uniform uvec4 SkyColors[16];
layout (location = 20) uniform uvec4 BlockColors[16];

vec3 gamma(vec3 color) {
  return pow(color, vec3(1.0 / 2.0));
}

void main() {
    vec3 materialDiffuseColor = pow(vec3(Color.rgb / 255), vec3(2.0));

    // Block Lighting
    vec3 blockLightColor = pow(vec3(BlockColors[BlockLightLevel]) / 255, vec3(2.0));
    float blockLightPower = (blockLightColor.r + blockLightColor.g + blockLightColor.b) / 3;
    vec3 blockLight = blockLightPower * blockLightColor * materialDiffuseColor;

    // Sky Lighting
    vec3 skyLightColor = pow(vec3(SkyColors[SkyLightLevel]) / 255, vec3(2.0));
    float skyLightPower = (skyLightColor.r + skyLightColor.g + skyLightColor.b) / 3;
    vec3 skyLight = skyLightPower * skyLightColor * materialDiffuseColor;

    // Directional Lighting
    float dirLightCoefficient = 0.01 * skyLightPower * clamp(dot(Normal, lightDir), 0.0, 1.0);
    vec3 dirLightColor = dirLightCoefficient * skyLightColor;

    // Ambient Occlusion
    vec3 inside = vec3(1.0);
    vec3 outside = vec3(0.75);
    vec3 ambientCoefficient = mix(outside, inside, Occlusion);

    color = vec4(gamma(ambientCoefficient * vec3(dirLightColor + skyLight + blockLight)), (Color.a / 255));
}