#version 150

uniform vec4 ColorModulator;

out vec4 fragColor;

void main() {
    fragColor = ColorModulator;
    gl_FragDepth = 1.0;
}
