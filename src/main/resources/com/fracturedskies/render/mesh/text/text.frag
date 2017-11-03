#version 450 core

in vec4 Color;
out vec4 color;

void main()
{
    color = Color / 255;
}
