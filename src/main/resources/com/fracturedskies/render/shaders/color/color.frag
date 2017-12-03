#version 450 core

out vec4 color;

in vec3 Color;
in vec3 Normal;

layout (location = 3) uniform sampler2DArray albedo;

void main()
{
    float ambientLight = 0.3;

    vec3 lightDir = normalize(vec3(1, 0.5, -0.25));
    float diffuse = 0.7 * max(dot(Normal, lightDir), 0.0);

    color = vec4((ambientLight + diffuse) * vec3(Color.rgb / 255), 1);
}
