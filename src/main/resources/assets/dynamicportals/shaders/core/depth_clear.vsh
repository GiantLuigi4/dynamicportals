#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

void main() {
    vec4 crd = ProjMat * ModelViewMat * vec4(Position, 1.0);
    crd.z = crd.w;
    gl_Position = crd;
}
