#version 150

uniform vec4 ColorModulator;

out vec4 fragColor;

void main() {
    fragColor = vec4(0, 0, 0, 1);
    gl_FragDepth = 1.0;
}
