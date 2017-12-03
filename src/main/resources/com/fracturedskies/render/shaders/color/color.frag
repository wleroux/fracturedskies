#version 450 core
out vec4 color;

in vec3 Color;
in vec3 Normal;
in float Occlusion;
in float SkyLightLevel;

layout (location = 3) uniform sampler2DArray albedo;

vec3 gamma(vec3 color){
    return pow(color, vec3(1.0/2.0));
}

void main()
{
    vec3 albedo = vec3(Color.rgb / 255);

    // Ambient Occlusion
    vec3 inside = vec3(0.2);
    vec3 outside = vec3(0.1);
    vec3 ambient = mix(outside, inside, Occlusion);

    // Directional Lighting
    float light = 0.4 * (SkyLightLevel / 16);
    float overheadLight;
    if (SkyLightLevel >= 15.5) {
        vec3 lightDir = normalize(vec3(0.6, 1, -0.25));
        overheadLight = 0.4 * max(dot(Normal, lightDir), 0.0);
    } else {
        overheadLight = 0;
    }

    color = vec4(vec3(gamma(
      ambient * albedo +
      (overheadLight + light) * albedo
      )), 1
    );
}
