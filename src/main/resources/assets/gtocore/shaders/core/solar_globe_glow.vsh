#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out float vertexDistance;
out vec2 glowCoord;

void main() {
    vec4 center = ModelViewMat * vec4(Position, 1.0);
    vec4 clipCenter = ProjMat * center;
    if (clipCenter.w <= 0.0 || clipCenter.z <= -clipCenter.w) {
        gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
        vertexDistance = 0.0;
        glowCoord = vec2(2.0);
        return;
    }
    // Expand in view space so the halo faces the viewer in the world and item previews.
    center.xy += UV0 * length(ModelViewMat[0].xyz);
    gl_Position = ProjMat * center;
    vertexDistance = fog_distance(ModelViewMat, Position, FogShape);
    glowCoord = sign(UV0);
}
