#version 450 core
out vec4 color;

in vec3 Color;
in vec3 Normal;
in float Occlusion;

layout (location = 3) uniform sampler2DArray albedo;

void main()
{
    vec3 albedo = vec3(Color.rgb / 255);

    // Ambient Occlusion
    vec3 inside = vec3(0.15);
    vec3 outside = vec3(0.05);
    vec3 ambient = 0.3 + mix(outside, inside, Occlusion);

    // Directional Lighting
    vec3 lightDir = normalize(vec3(0.6, 1, -0.25));
    float light = 0.5 * max(dot(Normal, lightDir), 0.0);

    color = vec4(
      ambient * albedo +
      light * albedo,
      1
    );
}
