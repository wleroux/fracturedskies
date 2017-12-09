#version 450 core
out vec4 color;

in vec4 Color;
in vec3 Normal;
in float Occlusion;
flat in int SkyLightLevel;

layout (location = 3) uniform vec3 lightDir;
layout (location = 4) uniform uvec4 SkyColors[16];

void main() {
    vec3 materialDiffuseColor = vec3(Color.rgb / 255);

    // Sky Lighting
    vec3 lightColor = vec3(SkyColors[SkyLightLevel]) / 255;
    float lightPower = (lightColor.r + lightColor.g + lightColor.b) / 3;
    vec3 light = 0.70 * lightPower * lightColor * materialDiffuseColor;

    // Directional Lighting
    float dirLightCoefficient = 0.1 * lightPower * clamp(dot(Normal, lightDir), 0.0, 1.0);
    vec3 dirLightColor = dirLightCoefficient * lightColor;

    // Ambient Occlusion
    vec3 inside = vec3(0.2);
    vec3 outside = vec3(0.1);
    vec3 ambientCoefficient = mix(outside, inside, Occlusion);
    vec3 ambientDiffuseColor = vec3(ambientCoefficient) * materialDiffuseColor;

    color = vec4(vec3(ambientDiffuseColor + dirLightColor + light), (Color.a / 255));
}