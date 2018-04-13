#version 450 core
out vec4 color;

in vec4 Color;
in vec3 Normal;
in float Occlusion;
in vec3 BlockLightColor;
in vec3 SkyLightColor;

layout (location = 3) uniform vec3 lightDir;

vec3 gamma(vec3 color) {
  return pow(color, vec3(1.0 / 2.0));
}

void main() {
    vec3 materialDiffuseColor = pow(vec3(Color.rgb / 255), vec3(2.0));

    // Block Lighting
    vec3 blockLightColor = BlockLightColor;
    float blockLightPower = (blockLightColor.r + blockLightColor.g + blockLightColor.b) / 3;
    vec3 blockLight = blockLightPower * blockLightColor * materialDiffuseColor;

    // Sky Lighting
    vec3 skyLightColor = SkyLightColor;
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